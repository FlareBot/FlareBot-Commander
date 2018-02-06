package stream.flarebot.flarebot_loader.rest;

import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Service;
import stream.flarebot.flarebot_loader.FlareBotLoader;
import stream.flarebot.flarebot_loader.modules.Module;
import stream.flarebot.flarebot_loader.modules.ModuleLoader;

import java.util.function.Consumer;
import java.util.function.Function;

public class ModuleRest {

    public ModuleRest(Service service) {
        service.get("/status/:id", this::handleStatus);
        service.get("/restart/:id", (req, res) -> handleAction(req, res, ModuleLoader.ModuleAction.RESTART));
        service.get("/stop/:id", (req, res) -> handleAction(req, res, ModuleLoader.ModuleAction.STOP));
        service.get("/start/:id", (req, res) -> handleAction(req, res, ModuleLoader.ModuleAction.START));
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
        Module module = FlareBotLoader.getInstance().getModuleLoader().getModule(req.params("id"));
        if (module == null) {
            return handleResponse(res, 400, getErrorJson("That module does not exist or is not loaded!"));
        }
        return handleResponse(res, 200, new JSONObject().put("status", module.getStatus().toString()));
    }

    private JSONObject handleAction(Request req, Response res, Consumer<Module> action) {
        Module module = FlareBotLoader.getInstance().getModuleLoader().getModule(req.params("id"));
        if (module == null) {
            return handleResponse(res, 400, getErrorJson("That module does not exist or is not loaded!"));
        }
        action.accept(module);
        return handleResponse(res, 200, new JSONObject().put("status", module.getStatus().toString()));
    }
}
