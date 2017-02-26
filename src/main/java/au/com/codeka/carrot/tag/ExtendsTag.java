package au.com.codeka.carrot.tag;

import au.com.codeka.carrot.CarrotEngine;
import au.com.codeka.carrot.CarrotException;
import au.com.codeka.carrot.expr.Statement;
import au.com.codeka.carrot.expr.StatementParser;
import au.com.codeka.carrot.Scope;
import au.com.codeka.carrot.resource.ResourceName;
import au.com.codeka.carrot.tmpl.Node;
import au.com.codeka.carrot.tmpl.TagNode;
import au.com.codeka.carrot.util.Log;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import static au.com.codeka.carrot.util.Preconditions.checkNotNull;

/**
 * The "extends" tag is used to base one template off of another one.
 *
 * <p>You would make a "skeleton" template like so:
 * <code>
 *   &lt;html&gt;
 *     &lt;head&gt;
 *       &lt;title&gt;Page title&lt;/title&gt;
 *     &lt;/head&gt;
 *     &lt;body&gt;
 *       {% block "content" %}foo{% end %}
 *     &lt;/body&gt;
 *   &lt;/html&gt;
 * </code>
 *
 * <p>And another file to "extend" it, like so:
 * <code>
 *   {% extends "skeleton.html" %}
 *   {% block "content" %}
 *     bar
 *   {% end %}
 * </code>
 *
 * <p>The contents of the second file will then be the contents of the skeleton file, and the "content" block will
 * be replaced with the content inside the block.
 */
public class ExtendsTag extends Tag {
  private Statement skeletonNameStatement;

  @Override
  public String getTagName() {
    return "extends";
  }

  @Override
  public boolean isBlockTag() {
    return true;
  }

  @Override
  public Tag clone() {
    return new ExtendsTag();
  }

  @Override
  public void parseStatement(StatementParser stmtParser) throws CarrotException {
    skeletonNameStatement = stmtParser.parseStatement();
  }

  @Override
  public void render(CarrotEngine engine, Writer writer, TagNode tagNode, Scope scope)
      throws CarrotException, IOException {
    String skeletonName = skeletonNameStatement.evaluate(engine.getConfig(), scope).toString();

    // We take our children, which should all be block tags, add them to a special variable in the scope, and then
    // just render the template instead.
    Map<String, TagNode> blockTags = new HashMap<>();

    checkNotNull(tagNode.getChildren());
    for (Node childNode : tagNode.getChildren()) {
      if (!(childNode instanceof TagNode)) {
        Log.warning(engine.getConfig(), "Unexpected node inside {%% extends %%}: %s", childNode);
        continue;
      }

      TagNode childTagNode = (TagNode) childNode;
      if (!(childTagNode.getTag() instanceof BlockTag)) {
        Log.warning(engine.getConfig(), "Unexpected tag instde {%% extends %%}: {%% %s %%}", childTagNode.getTag());
        continue;
      }

      BlockTag blockTag = (BlockTag) childTagNode.getTag();
      blockTags.put(blockTag.getBlockName(engine, scope), childTagNode);
    }

    // TODO: we should locate the resource with the current parent.
    ResourceName resourceName = engine.getConfig().getResourceLocater().findResource(null, skeletonName);

    Map<String, Object> context = new HashMap<>();
    context.put("__blocks", blockTags);
    scope.push(context);
    engine.process(writer, resourceName, scope);
    scope.pop();
  }
}