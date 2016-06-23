package edu.ksu.canvas.impl;

import com.google.common.collect.ImmutableMap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import edu.ksu.canvas.enums.EnrollmentType;
import edu.ksu.canvas.exception.InvalidOauthTokenException;
import edu.ksu.canvas.interfaces.CourseReader;
import edu.ksu.canvas.interfaces.EnrollmentsReader;
import edu.ksu.canvas.interfaces.EnrollmentsWriter;
import edu.ksu.canvas.model.Enrollment;
import edu.ksu.canvas.net.Response;
import edu.ksu.canvas.net.RestClient;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class EnrollmentsImpl extends BaseImpl<Enrollment, EnrollmentsReader, EnrollmentsWriter> implements EnrollmentsReader,EnrollmentsWriter {
    private static final Logger LOG = Logger.getLogger(CourseReader.class);

    public EnrollmentsImpl(String canvasBaseUrl, Integer apiVersion, String oauthToken, RestClient restClient, int connectTimeout, int readTimeout, Integer paginationPageSize) {
        super(canvasBaseUrl, apiVersion, oauthToken, restClient, connectTimeout, readTimeout, paginationPageSize);
    }

    @Override
    public List<Enrollment> getUserEnrollments(String userId) throws InvalidOauthTokenException, IOException {
        String createdUrl = buildCanvasUrl("users/" + userId + "/enrollments", Collections.emptyMap());
        LOG.debug("create URl for get enrollments for user : "+ createdUrl);
        return retrieveEnrollments(oauthToken, createdUrl);
    }

    @Override
    public List<Enrollment> getSectionEnrollments(String sectionId, List<EnrollmentType> enrollmentTypes) throws InvalidOauthTokenException, IOException {
        Map<String, List<String>> parameters = buildParameters(enrollmentTypes);
        String createdUrl = buildCanvasUrl("sections/" + sectionId + "/enrollments", parameters);
        LOG.debug("create URl for get enrollments for section : "+ createdUrl);
        return retrieveEnrollments(oauthToken, createdUrl);
    }

    @Override
    public Optional<Enrollment> enrollUser(String courseId, String userId) throws InvalidOauthTokenException, IOException {
        Map<String,String> courseMap = new HashMap<>();
        courseMap.put("enrollment[user_id]", String.valueOf(userId));
        String createdUrl = buildCanvasUrl("courses/" + courseId + "/enrollments", Collections.emptyMap());
        LOG.debug("create URl for course enrollments: "+ createdUrl);
        Response response = canvasMessenger.sendToCanvas(oauthToken, createdUrl, courseMap);
        if (response.getErrorHappened() ||  response.getResponseCode() != 200) {
            LOG.debug("Failed to enroll in course, error message: " + response.toString());
            return Optional.empty();
        }
        return responseParser.parseToObject(Enrollment.class,response);
    }

    @Override
    public Optional<Enrollment> dropUser(String courseId, String enrollmentId) throws InvalidOauthTokenException, IOException {
        Map<String,String> postParams = new HashMap<>();
        postParams.put("task", "delete");
        String createdUrl = buildCanvasUrl("courses/" + courseId + "/enrollments/" + enrollmentId, Collections.emptyMap());
        LOG.debug("create URl for course enrollments: "+ createdUrl);
        Response response = canvasMessenger.deleteFromCanvas(oauthToken, createdUrl, postParams);
        if (response.getErrorHappened() ||  response.getResponseCode() != 200) {
            LOG.debug("Failed to enroll in course, error message: " + response.toString());
            return Optional.empty();
        }
        return responseParser.parseToObject(Enrollment.class, response);
    }

    private List<Enrollment> retrieveEnrollments(String oauthToken, String url) throws IOException {
        List<Response> responses = canvasMessenger.getFromCanvas(oauthToken, url);
        return responses.stream().
                map(this::parseEnrollmentList).
                flatMap(Collection::stream).
                collect(Collectors.toList());
    }

    private Map<String, List<String>> buildParameters(List<EnrollmentType> enrollmentTypes) {
        return ImmutableMap.<String,List<String>>builder()
                .put("type[]", enrollmentTypes.stream().map(EnrollmentType::canvasValue).collect(Collectors.toList()))
                .build();
    }

    private List<Enrollment> parseEnrollmentList(Response response) {
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        Type listType = new TypeToken<List<Enrollment>>(){}.getType();
        return gson.fromJson(response.getContent(), listType);
    }

    @Override
    protected Type listType() {
        return new TypeToken<List<Enrollment>>(){}.getType();
    }

    @Override
    protected Class<Enrollment> objectType() {
        return Enrollment.class;
    }

}
