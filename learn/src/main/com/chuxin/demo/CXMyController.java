package chuxin.demo;

import chuxin.annotation.*;
import chuxin.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: learn
 * @description
 * @author: weis
 * @create: 2019-09-18 19:30
 **/
@CXController
@CXRequestMapping("/web")
public class CXMyController {
    @CXAutowired
    private CXMyService cxMyService;
    @CXAutowired(value = "myName")
    private CXNameService cxNameService;

    @CXRequestMapping("/query.json")
    @CXResponseBody
    public ModelAndView query(HttpServletRequest request, HttpServletResponse response,
                              @CXRequestParam(value = "name" ,required = false) String name)throws Exception{
        Map<String,Object> model = new HashMap<>();
        model.put("name",name);
        return new ModelAndView("first.cxml",model);
    }


    public void out(HttpServletRequest request, HttpServletResponse response,String str)throws Exception{
        response.getWriter().write(str);
    }

















}
