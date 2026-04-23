package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    // GET /api/v1/sensors?type=CO2 — List all (optionally filtered)
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensors = new ArrayList<>(store.getSensors().values());
        if (type != null && !type.isEmpty()) {
            sensors = sensors.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }
        return Response.ok(sensors).build();
    }

    // POST /api/v1/sensors — Register new sensor (validates roomId exists)
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor.getRoomId() == null || !store.getRooms().containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                    "The roomId '" + sensor.getRoomId() + "' does not exist in the system.");
        }
        if (sensor.getId() == null || sensor.getId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Sensor ID is required")).build();
        }
        store.getSensors().put(sensor.getId(), sensor);

        // Link sensor to room
        Room room = store.getRooms().get(sensor.getRoomId());
        room.getSensorIds().add(sensor.getId());

        return Response.created(URI.create("/api/v1/sensors/" + sensor.getId()))
                .entity(sensor).build();
    }

    // GET /api/v1/sensors/{sensorId}
    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) throw new ResourceNotFoundException("Sensor not found: " + sensorId);
        return Response.ok(sensor).build();
    }

    // Sub-resource locator — delegates to SensorReadingResource
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) throw new ResourceNotFoundException("Sensor not found: " + sensorId);
        return new SensorReadingResource(sensorId);
    }
}