package edu.ksu.canvas.impl;

import com.google.gson.reflect.TypeToken;
import edu.ksu.canvas.interfaces.CourseReader;
import edu.ksu.canvas.interfaces.CourseWriter;
import edu.ksu.canvas.model.Course;
import edu.ksu.canvas.model.Deposit;
import edu.ksu.canvas.model.Progress;
import edu.ksu.canvas.model.status.Conclude;
import edu.ksu.canvas.model.status.Delete;
import edu.ksu.canvas.net.Response;
import edu.ksu.canvas.net.RestClient;
import edu.ksu.canvas.oauth.OauthToken;
import edu.ksu.canvas.requestOptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class CourseImpl extends BaseImpl<Course, CourseReader, CourseWriter> implements CourseReader, CourseWriter {
    private static final Logger LOG = LoggerFactory.getLogger(CourseReader.class);

    public CourseImpl(String canvasBaseUrl, Integer apiVersion, OauthToken oauthToken, RestClient restClient,
                      int connectTimeout, int readTimeout, Integer paginationPageSize, Boolean serializeNulls) {
        super(canvasBaseUrl, apiVersion, oauthToken, restClient, connectTimeout, readTimeout,
                paginationPageSize, serializeNulls);
    }

    @Override
    public List<Course> listCurrentUserCourses(ListCurrentUserCoursesOptions options) throws IOException {
        LOG.debug("listing courses for user");
        String url = buildCanvasUrl("courses/", options.getOptionsMap());
        return getListFromCanvas(url);
    }

    public List<Course> listUserCourses(ListUserCoursesOptions options) throws  IOException {
        LOG.debug("listing course for user");
        String url = buildCanvasUrl("users/" + options.getUserId() + "/courses", options.getOptionsMap());
        return getListFromCanvas(url);
    }

    @Override
    public Optional<Course> getSingleCourse(GetSingleCourseOptions options) throws IOException {
        LOG.debug("getting course " + options.getCourseId());
        String path = "";
        String accountId = options.getAccount();
        if (accountId != null) {
            path += "accounts/"+accountId+ "/courses/";
        } else {
            path = "courses/";
        }
        String url = buildCanvasUrl(path + options.getCourseId(), options.getOptionsMap());
        LOG.debug("Final URL of API call: " + url);

        return retrieveCourseFromCanvas(oauthToken, url);
    }

    @Override
    public Optional<Course> getSingleCourse(String accountId, GetSingleCourseOptions options) throws IOException {
        LOG.debug("getting course " + options.getCourseId() + " in account "+ accountId);
        String url = buildCanvasUrl("accounts/"+ accountId+ "/courses/"+ options.getCourseId(), options.getOptionsMap());
        LOG.debug("Final URL of API call: " + url);
        return retrieveCourseFromCanvas(oauthToken, url);
    }

    private Optional<Course> retrieveCourseFromCanvas(OauthToken oauthToken, String url) throws IOException {
        Response response = canvasMessenger.getSingleResponseFromCanvas(oauthToken, url);
        if (response.getErrorHappened() || response.getResponseCode() != 200) {
            return Optional.empty();
        }
        return responseParser.parseToObject(Course.class, response);
    }

    @Override
    public Optional<Course> createCourse(String accountId, Course course) throws IOException {
        LOG.debug("creating course");
        String url = buildCanvasUrl("accounts/" + accountId + "/courses", Collections.emptyMap());
        Response response = canvasMessenger.sendJsonPostToCanvas(oauthToken, url, course.toJsonObject(serializeNulls));
        return responseParser.parseToObject(Course.class, response);
    }

    @Override
    public Optional<Course> updateCourse(Course course) throws IOException {
        LOG.debug("updating course");
        String url = buildCanvasUrl("courses/" + course.getId(), Collections.emptyMap());
        Response response = canvasMessenger.sendJsonPutToCanvas(oauthToken, url, course.toJsonObject(serializeNulls));
        return responseParser.parseToObject(Course.class, response);
    }

    @Override
    public Optional<Course> updateCourse(String id, Course course) throws IOException {
        LOG.debug("updating course");
        // TODO At some point we need to sort this out better throughout the library
        String url = buildCanvasUrl("courses/" + encode(id), Collections.emptyMap());
        Response response = canvasMessenger.sendJsonPutToCanvas(oauthToken, url, course.toJsonObject(serializeNulls));
        return responseParser.parseToObject(Course.class, response);
    }

    @Override
    public Optional<Progress> updateCourses(String accountId, UpdateCoursesOptions updateCoursesOptions) throws IOException {
        String url = buildCanvasUrl("accounts/"+ accountId+ "/courses", Collections.emptyMap());
        Response response = canvasMessenger.putToCanvas(oauthToken, url, updateCoursesOptions.getOptionsMap());

        Progress progress = GsonResponseParser.getDefaultGsonParser(serializeNulls).fromJson(response.getContent(), Progress.class);
        LOG.debug("ProgressId from update courses response: " + progress.getId());

        return Optional.of(progress);
    }

    @Override
    public Boolean deleteCourse(String courseId) throws IOException {
        Map<String, List<String>> postParams = new HashMap<>();
        postParams.put("event", Collections.singletonList("delete"));
        String createdUrl = buildCanvasUrl("courses/" + courseId, Collections.emptyMap());
        Response response = canvasMessenger.deleteFromCanvas(oauthToken, createdUrl, postParams);
        LOG.debug("response "+ response.toString());
        if (response.getErrorHappened() || response.getResponseCode() != 200) {
            LOG.debug("Failed to delete course, error message: " + response.toString());
            return false;
        }
        Optional<Delete> responseParsed = responseParser.parseToObject(Delete.class, response);

        return responseParsed.map(r -> r.getDelete()).orElse(false);
    }

    @Override
    public Boolean deleteCourse(DeleteCourseOptions options) throws IOException {
        String path = "";
        String accountId = options.getAccountId();
        if (accountId != null) {
            path += "accounts/"+accountId+ "/courses/";
        } else {
            path = "courses/";
        }
        String url = buildCanvasUrl(path + options.getCourseId(), Collections.emptyMap());
        Response response = canvasMessenger.deleteFromCanvas(oauthToken, url, options.getOptionsMap());
        LOG.debug("response " + response.toString());
        if (response.getErrorHappened() || response.getResponseCode() != 200) {
            LOG.debug("Failed to delete course, error message: " + response.toString());
            return false;
        }

        // The response from Canvas depends on the value of the event parameter.
        if (options.getEventType() == DeleteCourseOptions.EventType.DELETE) {
            Optional<Delete> responseParsed = responseParser.parseToObject(Delete.class, response);
            return responseParsed.map(Delete::getDelete).orElse(false);
        } else if (options.getEventType() == DeleteCourseOptions.EventType.CONCLUDE) {
            Optional<Conclude> responseParsed = responseParser.parseToObject(Conclude.class, response);
            return responseParsed.map(Conclude::getConclude).orElse(false);
        } else {
            throw new IllegalArgumentException("Unknown Canvas response: " + response.getContent());
        }
    }

    @Override
    public List<Course> listActiveCoursesInAccount(ListActiveCoursesInAccountOptions options) throws IOException {
        String url = buildCanvasUrl("accounts/" + options.getAccountId() + "/courses", options.getOptionsMap());
        return getListFromCanvas(url);
    }

    @Override
    public Optional<Deposit> uploadFile(String courseId, UploadOptions uploadOptions) throws IOException {
        String url = buildCanvasUrl("courses/"+ courseId+ "/files", Collections.emptyMap());
        Response response = canvasMessenger.sendToCanvas(oauthToken, url, uploadOptions.getOptionsMap());
        return responseParser.parseToObject(Deposit.class, response);
    }

    @Override
    protected Type listType() {
        return new TypeToken<List<Course>>(){}.getType();
    }

    @Override
    protected Class<Course> objectType() {
        return Course.class;
    }

}
