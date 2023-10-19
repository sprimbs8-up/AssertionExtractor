package org.example;


import de.uni_passau.fim.se2.deepcode.toolbox.ast.model.statement.Statement;

import java.util.List;

public record Assertion(List<Integer> pos, Statement assertionStatement) {
    public Assertion(int pos, Statement assertionStatement){
        this(List.of(pos),assertionStatement);
    }
}
