package com.learnlab.content.lessons

import com.learnlab.content.LessonBlock
import com.learnlab.content.LessonBlock.CharacterSay.Avatar
import com.learnlab.content.LessonBlock.CharacterSay.Side
import com.learnlab.content.LessonBlock.Callout.Tone
import com.learnlab.content.LessonContent

/**
 * NCERT Class 6 Science · Chapter 3 · §3.3.3 · Activity 3.7 (pages 49–50)
 * Test for proteins — the copper sulfate + caustic soda violet reaction.
 */
val ProteinVioletLesson = LessonContent(
    read = listOf(
        LessonBlock.Heading(number = "3.3.3", level = 2, text = "Test for proteins"),
        LessonBlock.Paragraph(
            "**Proteins** are an important part of our food. Milk products and pulses are good sources of protein. " +
                "Sportspersons need proteins in larger quantities to build their muscles. People get proteins " +
                "from plants as well as animals.",
        ),
        LessonBlock.Paragraph(
            "Some excellent plant sources of protein are pulses, beans, peas and nuts. Animal sources " +
                "include milk, **paneer**, egg, fish and meat. Protein-rich foods help in growth and repair " +
                "of our body. These are, therefore, called **body-building foods**.",
        ),
        LessonBlock.Callout(
            tone = Tone.FACT, title = "More to know!",
            body = "Have you ever seen mushrooms? They grow mostly in dark and moist places. Edible " +
                "mushrooms are good sources of protein.",
        ),

        LessonBlock.Heading(level = 2, text = "Activity 3.7: Let us investigate"),
        LessonBlock.Paragraph(
            "This activity may be demonstrated by the teacher. The real lab procedure is:",
        ),
        LessonBlock.Paragraph(
            "Take the food items tested in previous activities. Make a paste or powder using pestle and mortar. " +
                "Put about half a teaspoon of each food item in a separate clean test tube.",
        ),
        LessonBlock.Paragraph(
            "Add 2–3 teaspoons of water to each test tube and shake well. " +
                "Add two drops of **copper sulfate solution** using a dropper. " +
                "Then add 10 drops of **caustic soda solution** to each tube. " +
                "Shake well and leave the test tubes undisturbed for a few minutes.",
        ),

        LessonBlock.Callout(
            tone = Tone.WARNING, title = "Precautions",
            body = "These chemicals are harmful and need to be handled with care. Do not touch any of " +
                "these chemicals unless asked to do so. If any chemical gets spilled on your body, " +
                "immediately wash the affected area with water. Do not put any of these chemicals into " +
                "your mouth, or try to smell them.",
        ),

        LessonBlock.Paragraph(
            "What did you observe? Did the content of some test tubes turn **violet**? " +
                "This violet colour indicates the presence of proteins in the food item.",
        ),
        LessonBlock.Question(
            prompt = "Predict which of these will turn violet: soya bean, paneer, egg white, peanuts, " +
                "peas, boiled rice, sugar, butter.",
        ),
        LessonBlock.Paragraph(
            "In the app version, the chemistry is safely simulated — tap **Do** to start. " +
                "Predict, add reagents, watch the colour.",
        ),
    ),
    reflect = listOf(
        LessonBlock.Heading(level = 1, text = "What you should have seen"),
        LessonBlock.Paragraph(
            "Soya bean, paneer, egg white, peanuts and peas turned violet — all rich in **protein**. " +
                "Rice, sugar and butter did not change colour.",
        ),
        LessonBlock.Callout(
            tone = Tone.SUCCESS, title = "Conclusion",
            body = "A violet colour with copper sulfate + caustic soda indicates the presence of protein.",
        ),
        LessonBlock.Paragraph(
            "The right amount of protein must be included in the diet of growing children for their " +
                "proper growth and development.",
        ),
        LessonBlock.Paragraph(
            "Notice something interesting: **peanuts showed up positive on both the fat test and the " +
                "protein test.** Any food we eat may contain multiple nutrients. That is the bigger lesson.",
        ),
        LessonBlock.Question(
            prompt = "Which of these food components are part of your daily diet?",
        ),
    ),
)
