package com.learnlab.content.lessons

import com.learnlab.content.LessonBlock
import com.learnlab.content.LessonContent

/**
 * NCERT Class 6 Science · Chapter 3 · §3.3.1 · Activity 3.5
 *
 * Read → builds up "what are nutrients, why test for them, what is starch".
 * Do   → the existing IodineStarchTest activity (predict → drop → reveal).
 * Reflect → the closing line from the textbook + a wider takeaway.
 *
 * Text and structure follow the NCERT 2026-27 reprint pages 39–48 closely
 * but rewritten for a screen-paced lesson, not a printed page.
 */
val IodineStarchLesson = LessonContent(
    read = listOf(
        LessonBlock.Heading(
            number = "3.3", level = 1,
            text = "How to Test Different Components of Food?",
        ),
        LessonBlock.Paragraph(
            "Let us find out which nutrients are present in various food items.",
        ),
        LessonBlock.Paragraph(
            "Some nutrients like **starch** (a type of carbohydrate), **fat** and " +
                "**protein** can be detected using fairly simple tests, while others " +
                "can be detected only in a well-equipped laboratory.",
        ),
        LessonBlock.Paragraph(
            "Let us explore how we can detect the presence of starch, fat and " +
                "protein in some food items.",
        ),

        LessonBlock.Heading(
            number = "3.3.1", level = 2,
            text = "Test for starch",
        ),

        LessonBlock.CharacterSay(
            who = "Mishti",
            text = "If carbohydrates give us energy, can we actually see them in the food we eat?",
            side = LessonBlock.CharacterSay.Side.LEFT,
            avatar = LessonBlock.CharacterSay.Avatar.STUDENT_GIRL,
        ),
        LessonBlock.CharacterSay(
            who = "Medu",
            text = "We can — by adding iodine. Iodine reacts with starch and turns a deep blue-black.",
            side = LessonBlock.CharacterSay.Side.RIGHT,
            avatar = LessonBlock.CharacterSay.Avatar.STUDENT_BOY,
        ),

        LessonBlock.Paragraph(
            "In this activity (Activity 3.5 in the textbook), we drop a little **diluted " +
                "iodine solution** onto each food item and look at the colour change.",
        ),

        LessonBlock.Figure(
            figureId = "fig-3-7-iodine-bench",
            caption = "Fig. 3.7 — Testing for the presence of starch in various food items.",
        ),

        LessonBlock.Callout(
            title = "How the test works",
            tone = LessonBlock.Callout.Tone.INFO,
            body = "Iodine molecules slip into the spiral structure of starch and trap " +
                "light differently. That trapped-light effect is what we see as the " +
                "blue-black colour. No starch present? No colour change.",
        ),

        LessonBlock.KeyTerm(
            term = "Starch",
            definition = "A common carbohydrate stored by plants — found in potato, rice, " +
                "wheat and many other staples. It is the body's main quick source of energy.",
        ),

        LessonBlock.Heading(level = 2, text = "Before you begin"),

        LessonBlock.Paragraph(
            "Have a guess first. For each food sample, ask: do I think there is starch in this? " +
                "Predict before you drop. Science is built on prediction → observation → revision, " +
                "not on copying the answer.",
        ),

        LessonBlock.Question(
            prompt = "Which two of these — potato, cucumber, butter, sugar — do you think " +
                "will turn blue-black when iodine touches them?",
        ),

        LessonBlock.Callout(
            title = "A puzzle to watch for",
            tone = LessonBlock.Callout.Tone.FACT,
            body = "Sugar IS a carbohydrate, but iodine does not react with it. Iodine only " +
                "reacts with starch, not with simple sugars. This is the classic textbook " +
                "puzzle — pay attention when you test sugar.",
        ),

        LessonBlock.Paragraph(
            "Tap **Do** in the tabs above to run the experiment. Drop iodine on each food " +
                "item one at a time. Match your prediction against the result.",
        ),
    ),

    reflect = listOf(
        LessonBlock.Heading(level = 1, text = "What you should have seen"),

        LessonBlock.Paragraph(
            "Potato, bread, rice and boiled chana all turned blue-black. They are rich " +
                "in starch — cereals, root vegetables and pulses store energy this way.",
        ),
        LessonBlock.Paragraph(
            "Cucumber, peanuts, butter and sugar did not change colour. Cucumber is mostly " +
                "water, peanuts and butter are fats, and sugar is a different kind of carbohydrate.",
        ),

        LessonBlock.Callout(
            title = "Conclusion",
            tone = LessonBlock.Callout.Tone.SUCCESS,
            body = "A blue-black colour with iodine indicates the presence of starch in the food.",
        ),

        LessonBlock.Heading(level = 2, text = "Going further"),

        LessonBlock.Paragraph(
            "Most food items you eat contain more than one nutrient. The next two activities " +
                "(Oily Patch Fat Test and Protein Violet Test) check the same set of foods " +
                "for **fat** and **protein**. When you compare the three tests, you will see " +
                "that — for example — peanuts show both protein and fat.",
        ),
        LessonBlock.Paragraph(
            "This is the textbook's bigger point: every meal we eat is a mixture. " +
                "A **balanced diet** is one where, across the day, all the components " +
                "are present in the right amounts.",
        ),

        LessonBlock.CharacterSay(
            who = "Dr Poshita",
            text = "Health is the Ultimate Wealth. Knowing what is in your food is the " +
                "first step to choosing what to eat.",
            side = LessonBlock.CharacterSay.Side.LEFT,
            avatar = LessonBlock.CharacterSay.Avatar.SCIENTIST,
        ),
    ),
)
