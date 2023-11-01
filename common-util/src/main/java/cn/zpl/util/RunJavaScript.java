package cn.zpl.util;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class RunJavaScript {

    /**
     * 单例的JavaScript解析引擎，存在bug，当多线程执行时返回结果错乱，加入TheadLocal、加锁（加锁无效），或者使用引擎池
     */

    public static ThreadLocal<ScriptEngine> engineThreadLocal = new ThreadLocal<>();


    public static void init() {
        if (engineThreadLocal.get() != null) {
            return;
        }
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine scriptEngine = manager.getEngineByName("js");
        if (scriptEngine == null) {
            throw new RuntimeException("获取JavaScript解析引擎失败");
        }
        engineThreadLocal.set(scriptEngine);
    }

    public static Object executeForAttribute(String script, String attributeName) throws ScriptException {
        init();
        engineThreadLocal.get().eval(script);
        return engineThreadLocal.get().getContext().getAttribute(attributeName);
    }

    public static Object executeForAttributeChain(String script, @NotNull String attributeChain) throws ScriptException {
        init();
        engineThreadLocal.get().eval(script);
        String[] attributes = attributeChain.split("\\.");
        ScriptContext context = engineThreadLocal.get().getContext();
        Object mirror = null;
        for (String attribute : attributes) {
            if (mirror == null) {
                mirror = context.getAttribute(attribute);
            } else {
                mirror = ((ScriptObjectMirror) mirror).get(attribute);
            }
        }
        return mirror;
    }

    private static ScriptObjectMirror getValue(ScriptObjectMirror mirror, String attribute) {
        return (ScriptObjectMirror) mirror.getMember(attribute);
    }

    public static ScriptContext getScriptContext(String script) throws ScriptException {
        init();
        engineThreadLocal.get().eval(script);
        return engineThreadLocal.get().getContext();
    }

    public void test() throws ScriptException {
        System.out.println(executeForAttribute("\n" +
                "        let img_data = \"JYWw5g9AzAjFAcAGCBWFA2KBOGEYDoArABzABpRJYFk1McJA+HUFcMgk8y6OJVDbXFgCYipIA===\"\n" +
                "        let page = \"1\"\n" +
                "        let manga_url = \"https://www.manhuacat.com/manga/32105.html\";\n" +
                "        let cur_url = \"https://www.manhuacat.com/manga/32105/521761.html\"\n" +
                "    ", "img_data"));
    }
}
