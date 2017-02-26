package au.com.codeka.carrot.tag;

import au.com.codeka.carrot.CarrotEngine;
import au.com.codeka.carrot.CarrotException;
import au.com.codeka.carrot.Scope;
import au.com.codeka.carrot.expr.Statement;
import au.com.codeka.carrot.expr.StatementParser;
import au.com.codeka.carrot.tmpl.TagNode;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * The {% block %} tag is used in conjunction with {% extends %}. See {@link ExtendsTag} for details.
 */
public class BlockTag extends Tag {
  private Statement blockName;

  public BlockTag() {
  }

  public BlockTag(Statement blockName) {
    this.blockName = blockName;
  }

  @Override
  public String getTagName() {
    return "block";
  }

  public boolean isBlockTag() {
    return true;
  }

  @Override
  public void parseStatement(StatementParser statementParser) throws CarrotException {
    blockName = statementParser.parseStatement();
  }

  @Override
  public BlockTag clone() {
    return new BlockTag(blockName);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void render(CarrotEngine engine, Writer writer, TagNode tagNode, Scope scope)
      throws CarrotException, IOException {
    // If there's blocks, we'll want to render the corresponding block from there.
    TagNode otherBlockTagNode = null;
    Map<String, TagNode> blocksObj = (Map<String, TagNode>) scope.resolve("__blocks");
    if (blocksObj != null) {
      otherBlockTagNode = blocksObj.get(getBlockName(engine, scope));
    }

    if (otherBlockTagNode != null) {
      otherBlockTagNode.renderChildren(engine, writer, scope);
    } else {
      tagNode.renderChildren(engine, writer, scope);
    }
  }

  public String getBlockName(CarrotEngine carrotEngine, Scope scope) throws CarrotException {
    return blockName.evaluate(carrotEngine.getConfig(), scope).toString();
  }
}