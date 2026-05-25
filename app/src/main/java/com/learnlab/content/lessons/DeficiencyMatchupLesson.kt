package com.learnlab.content.lessons

import com.learnlab.content.LessonBlock
import com.learnlab.content.LessonBlock.CharacterSay.Avatar
import com.learnlab.content.LessonBlock.CharacterSay.Side
import com.learnlab.content.LessonBlock.Callout.Tone
import com.learnlab.content.LessonContent

/**
 * NCERT Class 6 Science · Chapter 3 · §3.2 (Vitamins/Minerals section) + Fig 3.5
 * + Activity 3.4 (pages 42–45).
 *
 * Two case studies (scurvy + goitre) then the deficiency chart.
 */
val DeficiencyMatchupLesson = LessonContent(
    read = listOf(
        LessonBlock.Heading(level = 1, text = "Why do we need vitamins and minerals?"),
        LessonBlock.Paragraph(
            "Carbohydrates, fats and proteins are the bulk of what we eat. But food also contains tiny " +
                "amounts of other components that are absolutely essential: **vitamins** and **minerals**.",
        ),
        LessonBlock.Paragraph(
            "Why do you think we are advised to include fruits, vegetables and other plant-based foods " +
                "in our daily diet? Let us understand by reading two real cases from history.",
        ),

        LessonBlock.CaseStudy(
            number = 1,
            title = "Sailors and scurvy",
            body = "In earlier times, during long voyages, sailors often suffered from bleeding and " +
                "swollen gums. During a voyage in 1746, Scottish physician James Lind observed that " +
                "sailors who consumed lemons and oranges recovered from these symptoms. Bleeding and " +
                "swollen gums are symptoms of a disease called scurvy.",
        ),
        LessonBlock.Paragraph(
            "What do you **interpret** by reading Case 1? Lemons and oranges help in curing scurvy. " +
                "Scurvy is caused due to deficiency of Vitamin C. **Vitamin C** present in citrus fruits " +
                "like lemons and oranges helps in curing this disease.",
        ),

        LessonBlock.CaseStudy(
            number = 2,
            title = "Iodised salt and goitre",
            body = "In the 1960s, Indian scientists found that among the human population in the Himalayan " +
                "region and the Northern plains of India, symptoms of swelling at the front of the neck " +
                "were prevalent. As per norms of the Government of India, an effort was made to supplement " +
                "common salt with iodine for preparing iodised salt. Consumption of iodised salt visibly " +
                "reduced the above symptoms. These symptoms were due to a deficiency of iodine in the soil " +
                "of this region resulting in a lack of iodine in the local food and water supply. Swelling " +
                "at the front of the neck is a symptom of a disease called goitre.",
        ),
        LessonBlock.Paragraph(
            "**Iodised salt** is simply common salt mixed with required quantities of salts of iodine. " +
                "It's a small fortification that prevents a serious disease.",
        ),

        LessonBlock.Heading(number = "3.2", level = 2, text = "The deficiency chart (Fig. 3.5)"),
        LessonBlock.Paragraph(
            "The textbook gives a chart linking each vitamin and mineral to its functions, food sources, " +
                "the disease it prevents, and the symptoms of that disease. Six rows that matter:",
        ),
        LessonBlock.KeyTerm("Vitamin A — Loss of vision",
            "Sources: papaya, carrot, mango, milk. Deficiency → poor vision, night blindness."),
        LessonBlock.KeyTerm("Vitamin B₁ — Beriberi",
            "Sources: legumes, nuts, whole grains, seeds, milk products. Symptoms: tingling/burning in feet and hands."),
        LessonBlock.KeyTerm("Vitamin C — Scurvy",
            "Sources: amla, guava, green chilli, orange, lemon. Symptoms: bleeding gums, slow healing."),
        LessonBlock.KeyTerm("Vitamin D — Rickets",
            "Sources: sunlight, milk, butter, fish, eggs. Symptoms: soft and bent bones."),
        LessonBlock.KeyTerm("Iodine — Goitre",
            "Sources: seaweed, water chestnut (singhada), iodised salt. Symptom: swelling at the front of the neck."),
        LessonBlock.KeyTerm("Iron — Anaemia",
            "Sources: green leafy vegetables, beetroot, pomegranate. Symptoms: weakness, shortness of breath."),

        LessonBlock.Callout(
            tone = Tone.INFO, title = "What the activity asks",
            body = "Build the chain: pick a symptom, then the missing nutrient, then the food that brings it back. " +
                "Locking all six chains gives you a working mental map of nutrition.",
        ),
        LessonBlock.Paragraph("Tap **Do** above to start the chain-matching activity."),
    ),
    reflect = listOf(
        LessonBlock.Heading(level = 1, text = "What you should now be able to say"),
        LessonBlock.Paragraph(
            "Food components that provide energy, support growth, help repair and protect our body from " +
                "diseases, and maintain various bodily functions are called **nutrients**. The major " +
                "nutrients are carbohydrates, proteins, fats, vitamins and minerals.",
        ),
        LessonBlock.Paragraph(
            "Vitamins and minerals are also called **protective nutrients**. They protect our body " +
                "from diseases and keep us healthy.",
        ),
        LessonBlock.CharacterSay(
            who = "Mishti", avatar = Avatar.STUDENT_GIRL, side = Side.RIGHT,
            text = "Vitamin D can be naturally produced by our body upon exposure to sunlight!",
        ),
        LessonBlock.CharacterSay(
            who = "Medu", avatar = Avatar.STUDENT_BOY, side = Side.LEFT,
            text = "So sunlight + milk + a balanced diet → strong bones. Diet alone isn't the whole story.",
        ),
        LessonBlock.Callout(
            tone = Tone.SUCCESS, title = "Big idea",
            body = "Most deficiency diseases are preventable. Knowing the food source for each " +
                "nutrient is the simplest health insurance there is.",
        ),
    ),
)
