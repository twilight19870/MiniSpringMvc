package chuxin.servlet;

import chuxin.annotation.CXController;
import chuxin.annotation.CXRequestMapping;
import chuxin.annotation.CXRequestParam;
import chuxin.context.CXApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.spec.ECField;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: learn
 * @description
 * @author: weis
 * @create: 2019-09-18 16:09
 **/
public class CXDispatcherServlet extends HttpServlet {


    private static final String LOCATION = "contextConfigLocation";

    private Map<String, Handler> handlerMapping = new HashMap<String, Handler>();

    //private List<Handler> handlerMappings = new ArrayList<Handler>();

    private Map<Handler,HandlerAdapter> adapterMapping = new HashMap<Handler,HandlerAdapter>();

    private List<CXViewResolver> viewResolvers = new ArrayList<CXViewResolver>();
    //调用我们得controller方法
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("调用doPost");
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Exception,msg:" + Arrays.toString(e.getStackTrace()));
        }

    }


    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //先取出来一个Hadler,从HandLerMapping中取
        Handler handler = getHandler(req);
        if (null == handler) {
            resp.getWriter().write("404 NOT FOUND");
            return;
        }
        //再取出来一个适配器
        HandlerAdapter ha = getHandlerAdapter(handler);
        //再由适配器去调用具体的方法
        ModelAndView modelAndView = ha.handle(req, resp, handler);
        //返回视图
        applyDefaultViewName(resp,modelAndView);
    }

    private void applyDefaultViewName(HttpServletResponse resp, ModelAndView modelAndView) throws Exception{
        if( null == modelAndView){return;}
        if( viewResolvers.isEmpty()){return;}
        for(CXViewResolver viewResolver : viewResolvers){
            if(!modelAndView.getView().equals(viewResolver.getViewName())){
                continue;
            }
            String r =viewResolver.parse(modelAndView);
            if(r != null ){
                resp.getWriter().write(r);
                break;
            }
        }
    }

    private HandlerAdapter getHandlerAdapter(Handler handler) {
        if(adapterMapping.isEmpty()){
            return null;
        }
        return adapterMapping.get(handler);
    }

    private Handler getHandler(HttpServletRequest req) {
        //循环HandlerMapping
        if(handlerMapping.isEmpty()){
            return null;
        }
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath,"").replaceAll("/+","/");
        return handlerMapping.get(url);
      /*  for(Map.Entry<String,Handler> entry : handlerMapping.entrySet()){
            if(url.equals(entry.getKey())){
                return entry.getValue();
            }
        }*/
        //return null;
    }


    //初始化IOC容器
    @Override
    public void init(ServletConfig config) throws ServletException {
        /*初始化IOC容器*/
        CXApplicationContext context = new CXApplicationContext(LOCATION);
        Map<String, Object> ioc = context.getAll();
        System.out.println(ioc);
        System.out.println(ioc.get("cXMyController"));
        //请求参数解析
        initMultipartResolver(context);
        //多语言国际化
        initLocaleResolver(context);
        //主题View层
        initThemeResolver(context);

        /*重要*/
        //解析URL和Method关联关系
        initHandlerMappings(context);
        //适配器(匹配得过程)
        initHandlerAdapters(context);
        /*重要*/


        //异常解析
        initHandlerExceptionResolvers(context);
        //视图得转发(根据视图名字匹配到具体得模板)
        initRequestToViewNameTranslator(context);


        //解析模板中得内容,拿到服务器传过来得数据,生成HTML代码
        initViewResolvers(context);
        //策略组件初始化
        initFlashMapManager(context);
        System.out.println("CXSpringMVC is init");
    }

    private void initFlashMapManager(CXApplicationContext context) {

    }

    private void initViewResolvers(CXApplicationContext context) {
        //模板一般是不会放到WebRoot下
        String templateRoot = context.getConfig().getProperty("templateRoot");
        //加载模板个数存储到缓存中,检查模板中的语法错误
        //归根到底就是一个普通文件
        String rootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        File rooDir = new File(rootPath);
        for(File file : rooDir.listFiles()){
            viewResolvers.add(new CXViewResolver(file.getName(),file));
        }


    }

    private void initRequestToViewNameTranslator(CXApplicationContext context) {

    }


    private void initHandlerExceptionResolvers(CXApplicationContext context) {

    }



    /*适配器(匹配的过程),动态匹配参数,动态赋值*/
    private void initHandlerAdapters(CXApplicationContext context) {
        if(handlerMapping.isEmpty()){
            return;
        }
        //参数的类型作为key,参数的索引号作为值
        Map<String,Integer> paramMapping = new HashMap<String, Integer>();
        //只需要取出具体的某个方法
        for(Map.Entry<String,Handler> entry :handlerMapping.entrySet()){
            //把这个方法上面所有的参数获取到
            Class<?>[] parameterTypes = entry.getValue().method.getParameterTypes();
            //参数有顺序,通过反射没法拿到参数的名字
            for (int i = 0; i < parameterTypes.length ; i++) {
                Class<?> type = parameterTypes[i];
                if(type == HttpServletRequest.class || type == HttpServletResponse.class){
                    paramMapping.put(type.getName(),i);
                }
            }
            Annotation[][] parameterAnnotations = entry.getValue().method.getParameterAnnotations();
            for (int i = 0; i < parameterAnnotations.length ; i++) {
                for(Annotation a : parameterAnnotations[i]){
                    if(a instanceof CXRequestParam){
                        String paramName = ((CXRequestParam) a).value();
                        if( !"".equals(paramName.trim())){
                            paramMapping.put(paramName,i);
                        }

                    }
                }
            }
            adapterMapping.put(entry.getValue(),new HandlerAdapter(paramMapping));

        }
    }

    /*解析url和Method的关联关系*/
    private void initHandlerMappings(CXApplicationContext context) {
        //只要是由controller修饰的类里面的方法全部找出来.而且这个方法要加上RequestMapping,才能倍外界访问
        Map<String, Object> ioc = context.getAll();
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(CXController.class)) {
                continue;
            }
            String url = "";
            if (clazz.isAnnotationPresent(CXController.class)) {
                CXRequestMapping requestMapping = clazz.getAnnotation(CXRequestMapping.class);
                url = requestMapping.value();
            }
            Method[] methods = clazz.getMethods();
            if (methods.length == 0) {
                return;
            }
            for (Method method : methods) {
                if (!method.isAnnotationPresent(CXRequestMapping.class)) {
                    continue;
                }
                CXRequestMapping requestMapping = method.getAnnotation(CXRequestMapping.class);
                //String regex = (url + requestMapping.value()).replaceAll("/+","/");
                String mappingUrl = url + requestMapping.value();
                handlerMapping.put(mappingUrl, new Handler(entry.getValue(),method));
            }

        }

        //RequestMapping上会配置一个URL,那么一个URL对应一个方法,并且保存到Map中


    }

    private void initThemeResolver(CXApplicationContext context) {

    }

    private void initLocaleResolver(CXApplicationContext context) {

    }

    private void initMultipartResolver(CXApplicationContext context) {

    }

    /**
     * @Description: 方法适配器
     * @auther: weis
     * @date: 2019/9/18 17:14
     */
    private class HandlerAdapter {
        Map<String,Integer> paramMapping;

        public HandlerAdapter(Map<String, Integer> paramMapping) {
            this.paramMapping = paramMapping;
        }


        private ModelAndView handle(HttpServletRequest req, HttpServletResponse resp, Handler handler) throws Exception{
            //用handler调用URL对应的method
            Class<?>[] parameterTypes = handler.method.getParameterTypes();
            Map<String,String[]> parameterMap = req.getParameterMap();
            Object[] paramValues = new Object[parameterTypes.length];
            for(Map.Entry<String,String[]> param : parameterMap.entrySet()){
                String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll(",\\s", "");
                if( !this.paramMapping.containsKey(param.getKey())){
                    continue;
                }
                int index = this.paramMapping.get(param.getKey());
                paramValues[index] = castStringValue(value,parameterTypes[index]);
            }
            String reqName = HttpServletRequest.class.getName();
            if(this.paramMapping.containsKey(reqName)){
                Integer reqIndex = this.paramMapping.get(reqName);
                paramValues[reqIndex] = req;
            }
            String respName = HttpServletResponse.class.getName();
            if(this.paramMapping.containsKey(reqName)){
                Integer respIndex = this.paramMapping.get(respName);
                paramValues[respIndex] = resp;
            }
            boolean md = (handler.method.getReturnType() == ModelAndView.class);
            Object r = handler.method.invoke(handler.controller,paramValues);
            if(md){
                return (ModelAndView) r;
            }else {
                return null;
            }
        }

        private Object castStringValue(String value,Class<?> clazz){
            if(clazz == String.class){
                return value;
            }else if(clazz == Integer.class){
                return Integer.valueOf(value);
            }else if(clazz == int.class){
                return Integer.valueOf(value).intValue();
            }else {
                return null;
            }

        }









    }

    /**
     * @Description: HandlerMapping的定义
     * @auther: weis
     * @date: 2019/9/18 17:14
     */
    private class Handler {
        protected Object controller;
        protected Method method;
        protected Handler(Object controller, Method method) {
            this.controller = controller;
            this.method = method;
        }
    }

    private class CXViewResolver {
        private String viewName;
        private File file;

        public CXViewResolver() {
        }

        public CXViewResolver(String viewName, File file) {
            this.viewName = viewName;
            this.file = file;
        }

        protected String parse(ModelAndView mv)throws Exception{
            RandomAccessFile r = new RandomAccessFile((this.file), "r");
            String line = null;
            StringBuffer sb = new StringBuffer();
            while ( null != (line = r.readLine())){
                Matcher matcher = matcher(line);
                while ( matcher.find()){
                    for( int i = 1 ; i <= matcher.groupCount();i ++){
                        String group = matcher.group(i);
                        Map<String, Object> mode = mv.getMode();
                        Object o = mode.get(group);
                        if( null == o){
                            continue;
                        }
                        line = line.replaceAll("@\\{"+group+"\\}",o.toString());
                    }
                }
                sb.append(line);
            }
            return sb.toString();
        }

        private Matcher matcher(String str){
            Pattern pattern = Pattern.compile("@\\{(.+?)\\}",Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(str);
            return matcher;
        }



        public String getViewName() {
            return viewName;
        }

        public File getFile() {
            return file;
        }
    }













}
