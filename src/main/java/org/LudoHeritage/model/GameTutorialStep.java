package org.LudoHeritage.model;

import java.util.List;

public record GameTutorialStep(
        String title,
        String objective,
        String explanation,
        String boardHelp,
        String changeSummary,
        String practicePrompt,
        List<String> legend,
        List<String> beforeBoard,
        List<String> afterBoard
) {
}
