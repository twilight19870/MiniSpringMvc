package chuxin.context;

import chuxin.annotation.CXAutowired;
import chuxin.annotation.CXController;
import chuxin.annotation.CXService;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @program: learn
 * @description
 * @author: weis
 * @create: 2019-09-18 16:56
 **/
public class CXApplicationContext {
    private Map<String, Object> instanceMap = new ConcurrentHashMap<String, Object>();

    private List<String> classCache = new ArrayList<String>();
    private Properties config = new Properties();
    public CXApplicationContext(String location) {
        //先加载配置文件(springIOC :定位载入注册初始化注入)
        InputStream is = null;
        try {
            //定位
            is = this.getClass().getClassLoader().getResourceAsStream("application.properties");
            //载入

            config.load(is);
            //注册,吧所有的class找出来循环
            String packageName = config.getProperty("scanPackage");
            doRegister(packageName);
            // 初始化,循环class
            doCreateBean();
            //注入
            populate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doRegister(String packageName) {
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                doRegister(packageName + "." + file.getName());
            } else {
                classCache.add(packageName + "." + file.getName().replace(".class", "").trim());
            }
        }


    }

    private void doCreateBean() {
        //检查是否有注册信息
        if (null == classCache || classCache.size() == 0) {
            return;
        }
        try {
            for (String className : classCache){
                Class<?> clazz = Class.forName(className);
                //只要加了service,controller等注解都要初始化
                if(clazz.isAnnotationPresent(CXController.class)){
                    String id = lowerFirstChar(clazz.getSimpleName());
                    instanceMap.put(id,clazz.newInstance());
                }else if(clazz.isAnnotationPresent(CXService.class)){
                    CXService service = clazz.getAnnotation(CXService.class);
                    String id = service.value();
                    //如果自定义名字,用自定义名字
                    if( ! "".equals(id.trim())){
                        instanceMap.put(id,clazz.newInstance());
                        continue;
                    }
                    //如果是空的,就用默认规则
                    //如果这个类是接口可以根据接口匹配
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for(Class<?> i : interfaces){
                        instanceMap.put(i.getName(),clazz.newInstance());
                    }
                }else {
                    continue;
                }
            }
        }catch (Exception e){
         e.printStackTrace();
        }
    }

    private String lowerFirstChar(String str){
        char[] chars = str.toCharArray();
        chars[0] +=32 ;
        return String.valueOf(chars);
    }

    private void populate() {
        //首先要判断IOC中容器有没有东西
        if( instanceMap.isEmpty() ){
            return;
        }
        for(Map.Entry<String,Object> entry : instanceMap.entrySet()){
            //把所有的属性全部取出来,包括私有属性
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for(Field field : fields){
                if(!field.isAnnotationPresent(CXAutowired.class)){
                    continue;
                }
                CXAutowired autowired = field.getAnnotation(CXAutowired.class);
                String id = autowired.value().trim();
                if("".equals(id)){
                    id = field.getType().getName();
                }
                field.setAccessible(true);//把私有变量设置开放访问权限
                try {
                    field.set(entry.getValue(),instanceMap.get(id));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }


            }
        }

    }
    /*  public Object getBean(String name) {
        return null;
    }*/

    public Map<String, Object> getAll() {
        return instanceMap;
    }

    public Properties getConfig() {
        return config;
    }
}
