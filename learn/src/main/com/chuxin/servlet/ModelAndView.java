package chuxin.servlet;

import java.util.Map;

/**
 * @program: learn
 * @description
 * @author: weis
 * @create: 2019-09-24 15:58
 **/
public class ModelAndView {
    private String view;
    private Map<String,Object> mode;

    public ModelAndView() {
    }

    public ModelAndView(String view, Map<String, Object> mode) {
        this.view = view;
        this.mode = mode;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public Map<String, Object> getMode() {
        return mode;
    }

    public void setMode(Map<String, Object> mode) {
        this.mode = mode;
    }
}
