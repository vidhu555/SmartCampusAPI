package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET /api/v1/sensors/{sensorId}/readings
    @GET
    public Response getReadings() {
        List<SensorReading> readings = store.getReadingsForSensor(sensorId);
        return Response.ok(readings).build();
    }

    // POST /api/v1/sensors/{sensorId}/readings
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = store.getSensors().get(sensorId);

        // Block if MAINTENANCE
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId);
        }

        // Auto-assign id and timestamp if not set
        SensorReading newReading = new SensorReading(reading.getValue());

        // Store reading
        store.addReading(sensorId, newReading);

        // Side effect: update sensor's currentValue
        sensor.setCurrentValue(newReading.getValue());

        return Response.status(Response.Status.CREATED).entity(newReading).build();
    }
}