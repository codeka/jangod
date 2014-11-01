package au.com.codeka.carrot.interpret;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import au.com.codeka.carrot.base.Application;
import au.com.codeka.carrot.base.CarrotException;
import au.com.codeka.carrot.base.Configuration;
import au.com.codeka.carrot.base.Constants;
import au.com.codeka.carrot.base.Context;
import au.com.codeka.carrot.parse.TokenParser;
import au.com.codeka.carrot.tree.Node;
import au.com.codeka.carrot.tree.TreeParser;
import au.com.codeka.carrot.util.ListOrderedMap;
import au.com.codeka.carrot.util.ListOrderedMap.Item;
import au.com.codeka.carrot.util.Log;
import au.com.codeka.carrot.util.Variable;

public class CarrotInterpreter implements Cloneable {

  public static final String CHILD_FLAG = "'IS\"CHILD";
  public static final String PARENT_FLAG = "'IS\"PARENT";
  public static final String INSERT_FLAG = "'IS\"INSERT";
  public static final String SEMI_RENDER = "'SEMI\"FORMAL";
  public static final String BLOCK_LIST = "'BLK\"LIST";
  public static final String SEMI_BLOCK = "<K2C9OL7B>";

  static final String VAR_DATE = "now";
  static final String VAR_PATH = "workspace";

  private int level = 1;
  private FloorBindings runtime;
  private Context context;
  private Log log;
  String file = null;

  public CarrotInterpreter(Context context) {
    this.context = context;
    runtime = new FloorBindings();
    log = new Log(context.getApplication().getConfiguration());
  }

  private CarrotInterpreter() {
  }

  public Configuration getConfiguration() {
    return context.getConfiguration();
  }

  @Override
  public CarrotInterpreter clone() {
    CarrotInterpreter compiler = new CarrotInterpreter();
    compiler.context = context;
    compiler.runtime = runtime.clone();
    compiler.log = log;
    return compiler;
  }

  public void init() {
    runtime = new FloorBindings();
    level = 1;
  }

  public void render(TokenParser parser, Writer writer) throws CarrotException, IOException {
    render(new TreeParser(context.getApplication()).parse(parser), writer);
  }

  public void render(Node root, Writer writer) throws CarrotException, IOException {
    for (Node node : root.children()) {
      node.render(this, writer);
    }
    if (runtime.get(CHILD_FLAG, 1) != null &&
        runtime.get(INSERT_FLAG, 1) == null) {
      StringBuilder sb = new StringBuilder((String) fetchRuntimeScope(SEMI_RENDER, 1));
      // replace the block identify with block content
      ListOrderedMap blockList = (ListOrderedMap) fetchRuntimeScope(BLOCK_LIST, 1);
      Iterator<Item> mi = blockList.iterator();
      int index;
      String replace;
      Item item;
      while (mi.hasNext()) {
        item = mi.next();
        replace = SEMI_BLOCK + item.getKey();
        while ((index = sb.indexOf(replace)) > 0) {
          sb.delete(index, index + replace.length());
          sb.insert(index, item.getValue());
        }
      }
      writer.write(sb.toString());
    }
  }

  public Object retraceVariable(String variable) throws CarrotException {
    if (variable == null || variable.trim().length() == 0) {
      // No variable, just return empty string.
      return "";
    }
    Variable var = new Variable(variable);
    String varName = var.getName();
    // find from runtime(tree scope) > engine > global
    Object obj = runtime.get(varName, level);
    int lvl = level;
    while (obj == null && lvl > 1) {
      obj = runtime.get(varName, --lvl);
    }
    if (obj == null) {
      obj = context.getAttribute(varName);
    }
    if (obj == null) {
      if (VAR_DATE.equals(variable)) {
        return new java.util.Date();
      }
      if (VAR_PATH.equals(variable)) {
        return getWorkspace();
      }
    }
    if (obj != null) {
      obj = var.resolve(obj);
      if (obj == null) {
        log.warn("%s can't resolve member '%s'", varName, variable);
      }
    } else {
      log.warn("%s can't resolve variable '%s'", variable, varName);
    }
    return obj;
  }

  public String resolveString(String variable) throws CarrotException {
    if (variable == null || variable.trim().length() == 0) {
      throw new InterpretException("Variable name is required.");
    }
    if (variable.startsWith(Constants.STR_DOUBLE_QUOTE) ||
        variable.startsWith(Constants.STR_SINGLE_QUOTE)) {
      return variable.substring(1, variable.length() - 1);
    } else {
      Object val = retraceVariable(variable);
      if (val == null)
        return variable;
      return val.toString();
    }
  }

  public Object resolveObject(String variable) throws CarrotException {
    if (variable == null || variable.trim().length() == 0) {
      throw new InterpretException("Variable name is required.");
    }
    if (variable.startsWith(Constants.STR_DOUBLE_QUOTE) ||
        variable.startsWith(Constants.STR_SINGLE_QUOTE)) {
      return variable.substring(1, variable.length() - 1);
    } else {
      Object val = retraceVariable(variable);
      if (val == null)
        return variable;
      return val;
    }
  }

  /**
   * save variable to runtime tree scope space
   * 
   * @param name
   * @param item
   */
  public void assignRuntimeScope(String name, Object item) {
    runtime.put(name, item, level);
  }

  public void assignRuntimeScope(String name, Object item, int level) {
    runtime.put(name, item, level);
  }

  public Object fetchRuntimeScope(String name, int level) {
    return runtime.get(name, level);
  }

  public Object fetchRuntimeScope(String name) {
    return runtime.get(name, level);
  }

  public void setLevel(int lvl) {
    level = lvl;
  }

  public int getLevel() {
    return level;
  }

  public Context getContext() {
    return context;
  }

  public Application getApplication() {
    return context.getApplication();
  }

  public String getWorkspace() {
    if (file != null) {
      try {
        return context.getConfiguration().getResourceLocater().getDirectory(file);
      } catch (IOException e) { }
    }

    return null;
  }

  public void setFile(String fullName) {
    this.file = fullName;
  }
}
