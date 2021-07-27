package org.yaml.snakeyaml.nodes;

import java.util.Iterator;
import java.util.List;
import org.yaml.snakeyaml.error.Mark;

public class MappingNode extends CollectionNode<NodeTuple> {
   private List<NodeTuple> value;
   private boolean merged;

   public MappingNode(Tag tag, boolean resolved, List<NodeTuple> value, Mark startMark, Mark endMark, Boolean flowStyle) {
      super(tag, startMark, endMark, flowStyle);
      this.merged = false;
      if (value == null) {
         throw new NullPointerException("value in a Node is required.");
      } else {
         this.value = value;
         this.resolved = resolved;
      }
   }

   public MappingNode(Tag tag, List<NodeTuple> value, Boolean flowStyle) {
      this(tag, true, value, (Mark)null, (Mark)null, flowStyle);
   }

   public NodeId getNodeId() {
      return NodeId.mapping;
   }

   public List<NodeTuple> getValue() {
      return this.value;
   }

   public void setValue(List<NodeTuple> merge) {
      this.value = merge;
   }

   public void setOnlyKeyType(Class<? extends Object> keyType) {
      Iterator var2 = this.value.iterator();

      while(var2.hasNext()) {
         NodeTuple nodes = (NodeTuple)var2.next();
         nodes.getKeyNode().setType(keyType);
      }

   }

   public void setTypes(Class<? extends Object> keyType, Class<? extends Object> valueType) {
      Iterator var3 = this.value.iterator();

      while(var3.hasNext()) {
         NodeTuple nodes = (NodeTuple)var3.next();
         nodes.getValueNode().setType(valueType);
         nodes.getKeyNode().setType(keyType);
      }

   }

   public String toString() {
      StringBuilder buf = new StringBuilder();

      for(Iterator var3 = this.getValue().iterator(); var3.hasNext(); buf.append(" }")) {
         NodeTuple node = (NodeTuple)var3.next();
         buf.append("{ key=");
         buf.append(node.getKeyNode());
         buf.append("; value=");
         if (node.getValueNode() instanceof CollectionNode) {
            buf.append(System.identityHashCode(node.getValueNode()));
         } else {
            buf.append(node.toString());
         }
      }

      String values = buf.toString();
      return "<" + this.getClass().getName() + " (tag=" + this.getTag() + ", values=" + values + ")>";
   }

   public void setMerged(boolean merged) {
      this.merged = merged;
   }

   public boolean isMerged() {
      return this.merged;
   }
}
