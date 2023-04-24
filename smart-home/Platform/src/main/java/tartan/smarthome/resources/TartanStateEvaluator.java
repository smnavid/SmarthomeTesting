
package tartan.smarthome.resources;

import java.util.Map;

public interface TartanStateEvaluator {
    public Map<String, Object> evaluateState(Map<String, Object> inState, StringBuffer log);    
}