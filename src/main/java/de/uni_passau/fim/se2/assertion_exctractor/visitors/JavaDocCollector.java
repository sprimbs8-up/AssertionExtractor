package de.uni_passau.fim.se2.assertion_exctractor.visitors;

import de.uni_passau.fim.se2.assertion_exctractor.data.JavaDocMethod;
import de.uni_passau.fim.se2.assertion_exctractor.parsing.FocalMethodParser;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParser;
import de.uni_passau.fim.se2.deepcode.toolbox.ast.generated.JavaParserBaseVisitor;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;

import java.util.Optional;
import java.util.stream.Stream;

public class JavaDocCollector extends JavaParserBaseVisitor<Void> {
   private Stream.Builder<JavaDocMethod> builder =Stream.builder();

    @Override
    public Void visitClassBodyDeclaration(JavaParser.ClassBodyDeclarationContext ctx) {
        if(Optional.ofNullable(ctx.children).stream().noneMatch(ErrorNode.class::isInstance)) {
            var javaDocCtx = ctx.javadoc();
            var memberDeclaration = ctx.memberDeclaration();
            if (javaDocCtx != null && memberDeclaration != null && memberDeclaration.methodDeclaration() != null) {
                MethodTokenVisitor visitor = new MethodTokenVisitor();
                visitor.visitMethodDeclaration(memberDeclaration.methodDeclaration());
                Stream<String> c = Stream.concat(ctx.modifier().stream().map(RuleContext::getText), visitor.getCollectedTokens());
                builder.add(new JavaDocMethod(cleanJavaDoc(ctx.javadoc().getText()), c.toList()));
            }
        }
        return null;
    }
    private static String cleanJavaDoc(String rawJavaDoc){
        return rawJavaDoc.replaceAll("((\r)?\n( )*\\*)|/\\*\\*|/", " ")
                .replaceAll("\t"," ")
                .replaceAll("\n"," ")
                .replaceAll("\r"," ")
                .replaceAll(" +"," ")
                .strip();
    }

    public Stream<JavaDocMethod> getCollectedJavaDocs(){
        Stream<JavaDocMethod> builtStream =builder.build();
        builder = Stream.builder();
        return builtStream;
    }
}
