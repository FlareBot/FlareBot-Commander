package stream.flarebot.flarebot_loader.rest;

import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Service;
import stream.flarebot.flarebot_loader.FlareBotLoader;
import stream.flarebot.flarebot_loader.modules.Module;

public class RestTest {

    public RestTest(Service service) {
        service.get("/status/:id", this::handleStatus);
        service.get("/restart/:id", this::handleRestart);
    }

    private JSONObject getErrorJson(String message) {
        return new JSONObject().put("error", message);
    }

    private JSONObject handleResponse(Response response, int code, JSONObject returnVal) {
        response.status(code);
        response.type("application/json");
        response.body(returnVal.toString());
        return returnVal;
    }

    private JSONObject handleStatus(Request req, Response res) {
        Module module = FlareBotLoader.getInstance().getModuleLoader().getModuleById(req.params("id"));
        if (module == null) {
            return handleResponse(res, 400, getErrorJson("That module does not exist or is not loaded!"));
        }
        return handleResponse(res, 200, new JSONObject().put("status", module.getStatus().toString()));
    }

    private JSONObject handleRestart(Request req, Response res) {
        Module module = FlareBotLoader.getInstance().getModuleLoader().getModuleById(req.params("id"));
        if (module == null) {
            return handleResponse(res, 400, getErrorJson("That module does not exist or is not loaded!"));
        }
        FlareBotLoader.getInstance().getModuleLoader().restartModule(module);
        return handleResponse(res, 200, new JSONObject().put("status", module.getStatus().toString()));
    }
}
