package demo.servlet;


import com.sun.javafx.collections.MappingChange;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by yunfan on 2018/4/20.
 */
public class DemoServlet extends HttpServlet {

    Properties properties = new Properties();
    private static final long serialVersionUID = 1L;
    List<String> packageNames = new ArrayList<String>();
    // 所有类的实例，key是注解的value,value是所有类的实例
    Map<String, Object> instanceMap = new HashMap<String, Object>();
    Map<String, Object> handerMap = new HashMap<String, Object>();
    public DemoServlet() {
        super();
    }

    /**
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {

        // 读取配置文件
//        doReadProperties();
        // 包扫描,获取包中的文件
        scanPackage("com.chaoyue");
        try {
            filterAndInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 建立映射关系
//        handerMap();
        // 实现注入
        ioc();
    }

    public static void main(String[] args) {
        try {
            Class<?> cName = Class.forName("demo.servlet.DemoServlet");
            System.out.println(cName.newInstance());
        }catch (Exception e){

        }

    }
    private void filterAndInstance() throws Exception {
        if (packageNames.size() <= 0) {
            return;
        }
        for (String className : packageNames) {
            Class<?> cName = Class.forName(className.replace(".class", "").trim());
            if (cName.isAnnotationPresent(Controller.class)) {
                Object instance = cName.newInstance();
                Controller controller = (Controller) cName.getAnnotation(Controller.class);
                String key = controller.value();
                instanceMap.put(key, instance);
            } else if (cName.isAnnotationPresent(Service.class)) {
                Object instance = cName.newInstance();
                Service service = (Service) cName.getAnnotation(Service.class);
                String key = service.value();
                instanceMap.put(key, instance);
            } else {
                continue;
            }
        }
    }

    private void ioc() {
        if (instanceMap.isEmpty())
            return;
        for (Map.Entry<String, Object> entry : instanceMap.entrySet()) {
            // 拿到里面的所有属性
            Field fields[] = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);// 可访问私有属性
                if (field.isAnnotationPresent(Controller.class));
                Controller quatifier = field.getAnnotation(Controller.class);
                String value = quatifier.value();
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(), instanceMap.get(value));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 扫描包下的所有文件
     *
     * @param Package
     */
    private void scanPackage(String Package) {
        URL url = this.getClass().getClassLoader().getResource("/" + replaceTo(Package));// 将所有的.转义获取对应的路径
        String pathFile = url.getFile();
        File file = new File(pathFile);
        String fileList[] = file.list();
        for (String path : fileList) {
            File eachFile = new File(pathFile + path);
            if (eachFile.isDirectory()) {
                scanPackage(Package + eachFile.getName());
            } else {
                packageNames.add(Package + "." + eachFile.getName());
            }
        }
    }

//    /**
//     * 建立映射关系
//     */
//    private void handerMap() {
//        if (instanceMap.size() <= 0)
//            return;
//        for (Map.Entry<String, Object> entry : instanceMap.entrySet()) {
//            if (entry.getValue().getClass().isAnnotationPresent(Controller.class)) {
//                Controller controller = (Controller) entry.getValue().getClass().getAnnotation(Controller.class);
//                String ctvalue = controller.value();
//                Method[] methods = entry.getValue().getClass().getMethods();
//                for (Method method : methods) {
//                    if (method.isAnnotationPresent(RequestMapping.class)) {
//                        RequestMapping rm = (RequestMapping) method.getAnnotation(RequestMapping.class);
//                        String rmvalue = rm.value();
//                        handerMap.put("/" + ctvalue + "/" + rmvalue, method);
//                    } else {
//                        continue;
//                    }
//                }
//            } else {
//                continue;
//            }
//
//        }
//    }

    private String replaceTo(String path) {
        return path.replaceAll("\\.", "/");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doGet(req, resp);
    }

//    @Override
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        String url = req.getRequestURI();
//        String context = req.getContextPath();
//        String path = url.replace(context, "");
//        Method method = (Method) handerMap.get(path);
//        SpringmvcController controller = (SpringmvcController) instanceMap.get(path.split("/")[1]);
//        try {
//            method.invoke(controller, new Object[] { req, resp, null });
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
//    }
}
