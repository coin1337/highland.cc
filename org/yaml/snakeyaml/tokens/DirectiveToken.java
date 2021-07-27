package org.yaml.snakeyaml.tokens;

import java.util.List;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.error.YAMLException;

public final class DirectiveToken<T> extends Token {
   private final String name;
   private final List<T> value;

   public DirectiveToken(String name, List<T> value, Mark startMark, Mark endMark) {
      super(startMark, endMark);
      this.name = name;
      if (value != null && value.size() != 2) {
         throw new YAMLException("Two strings must be provided instead of " + String.valueOf(value.size()));
      } else {
         this.value = value;
      }
   }

   public String getName() {
      return this.name;
   }

   public List<T> getValue() {
      return this.value;
   }

   protected String getArguments() {
      return this.value != null ? "name=" + this.name + ", value=[" + this.value.get(0) + ", " + this.value.get(1) + "]" : "name=" + this.name;
   }

   public Token.ID getTokenId() {
      return Token.ID.Directive;
   }
}
