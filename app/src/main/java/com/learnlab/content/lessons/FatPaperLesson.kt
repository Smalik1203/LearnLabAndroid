package com.learnlab.content.lessons

import com.learnlab.content.LessonBlock
import com.learnlab.content.LessonBlock.CharacterSay.Avatar
import com.learnlab.content.LessonBlock.CharacterSay.Side
import com.learnlab.content.LessonBlock.Callout.Tone
import com.learnlab.content.LessonContent

/**
 * NCERT Class 6 Science · Chapter 3 · §3.3.2 · Activity 3.6 (page 49)
 * Test for fats — the oily patch / translucent paper test.
 */
val FatPaperLesson = LessonContent(
    read = listOf(
        LessonBlock.Heading(number = "3.3.2", level = 2, text = "Test for fats"),
        LessonBlock.Paragraph(
            "Carbohydrates and fats both give us energy, but they're stored very differently. " +
                "Fats can be detected with a simple paper test — no chemicals at all.",
        ),
        LessonBlock.CharacterSay(
            who = "Mishti", avatar = Avatar.STUDENT_GIRL, side = Side.LEFT,
            text = "Why do peanuts feel oily on the fingers but rice doesn't?",
        ),
        LessonBlock.CharacterSay(
            who = "Medu", avatar = Avatar.STUDENT_BOY, side = Side.RIGHT,
            text = "Because peanuts have a lot of fat. Let's see if we can prove it with just a piece of paper.",
        ),

        LessonBlock.Heading(level = 2, text = "Activity 3.6: Let us investigate"),
        LessonBlock.Paragraph(
            "Take a small piece of each food you tested for starch. Place it on a separate piece of paper. " +
                "Wrap the paper around the food and press it. Be careful not to tear the paper. " +
                "If a food contains some water, let the paper dry first.",
        ),
        LessonBlock.Paragraph(
            "Now hold the paper up against the light. **Can you see the light faintly shining through the patch?** " +
                "If yes, the food contains fat. An oily patch makes the paper translucent.",
        ),

        LessonBlock.KeyTerm(
            term = "Translucent",
            definition = "Lets some light pass through, even though you can't see clearly through it. " +
                "An oily patch on paper does exactly this.",
        ),

        LessonBlock.Callout(
            tone = Tone.INFO, title = "Why this works",
            body = "Paper is made of tiny criss-crossed plant fibres with air between them. " +
                "Air scatters light — paper looks white. Oil seeps into the gaps and replaces the " +
                "air, so light passes straighter through. That's the translucent patch you see.",
        ),

        LessonBlock.Question(
            prompt = "Predict which will leave an oily patch: butter, peanuts, coconut, cooking oil, " +
                "boiled rice, potato, cucumber, sugar.",
        ),
        LessonBlock.Paragraph(
            "Tap **Do** above and try each one. Make your prediction first, then press the paper, " +
                "then see what happens.",
        ),
    ),
    reflect = listOf(
        LessonBlock.Heading(level = 1, text = "What you should have seen"),
        LessonBlock.Paragraph(
            "Butter, peanuts, coconut and cooking oil all left clear oily patches — these foods are " +
                "rich in **fat**. Rice, potato, cucumber and sugar did not.",
        ),
        LessonBlock.Callout(
            tone = Tone.SUCCESS, title = "Conclusion",
            body = "An oily patch that makes the paper translucent indicates the presence of fat in the food.",
        ),
        LessonBlock.Paragraph(
            "Carbohydrates and fats provide us energy for performing various activities. Therefore, " +
                "they are called **energy-giving foods**.",
        ),
        LessonBlock.Callout(
            tone = Tone.FACT, title = "More to know!",
            body = "Polar bears accumulate a lot of fat under their skin. This fat serves as an energy " +
                "source — it supports them through months-long winter sleep (hibernation), enabling them " +
                "to survive without eating.",
        ),
        LessonBlock.CharacterSay(
            who = "Medu's grandma", avatar = Avatar.GRANDMA, side = Side.LEFT,
            text = "That's why I add ghee and nuts to your laddoos in winter — they give energy and keep you warm.",
        ),
    ),
)
