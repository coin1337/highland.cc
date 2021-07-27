package org.yaml.snakeyaml.nodes;

import java.util.Iterator;
import java.util.List;
import org.yaml.snakeyaml.error.Mark;

public class SequenceNode extends CollectionNode<Node> {
   private final List<Node> value;

   public SequenceNode(Tag tag, boolean resolved, List<Node> value, Mark startMark, Mark endMark, Boolean flowStyle) {
      super(tag, startMark, endMark, flowStyle);
      if (value == null) {
         throw new NullPointerException("value in a Node is required.");
      } else {
         this.value = value;
         this.resolved = resolved;
      }
   }

   public SequenceNode(Tag tag, List<Node> value, Boolean flowStyle) {
      this(tag, true, value, (Mark)null, (Mark)null, flowStyle);
   }

   public NodeId getNodeId() {
      return NodeId.sequence;
   }

   public List<Node> getValue() {
      return this.value;
   }

   public void setListType(Class<? extends Object> listType) {
      Iterator var2 = this.value.iterator();

      while(var2.hasNext()) {
         Node node = (Node)var2.next();
         node.setType(listType);
      }

   }

   public String toString() {
      return "<" + this.getClass().getName() + " (tag=" + this.getTag() + ", value=" + this.getValue() + ")>";
   }
}
