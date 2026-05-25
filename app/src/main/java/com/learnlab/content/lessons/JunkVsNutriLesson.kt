package com.learnlab.content.lessons

import com.learnlab.content.LessonBlock
import com.learnlab.content.LessonBlock.CharacterSay.Avatar
import com.learnlab.content.LessonBlock.CharacterSay.Side
import com.learnlab.content.LessonBlock.Callout.Tone
import com.learnlab.content.LessonContent

/**
 * NCERT Class 6 Science · Chapter 3 · Activity 3.9 (page 52) + §3.5 Millets (page 53).
 * Comparing potato wafers to roasted chana, then introducing nutri-cereals.
 */
val JunkVsNutriLesson = LessonContent(
    read = listOf(
        LessonBlock.Heading(level = 2, text = "Activity 3.9: Let us compare"),
        LessonBlock.Paragraph(
            "Read the nutritional information for a packet of **potato wafers** and a packet of " +
                "**roasted chana**. The textbook prints both labels side by side (per 100 g):",
        ),
        LessonBlock.KeyTerm(
            term = "Potato wafers (per 100 g)",
            definition = "Energy 536 kcal · Fats 35.0 g · Carbs 53.0 g · Proteins 7.0 g · Dietary fibre 4.8 g",
        ),
        LessonBlock.KeyTerm(
            term = "Roasted chana (per 100 g)",
            definition = "Energy 355 kcal · Fats 6.26 g · Carbs 58.58 g · Proteins 18.64 g · Dietary fibre 16.8 g",
        ),
        LessonBlock.Question(
            prompt = "Based on those numbers, which food would you choose? Why?",
        ),

        LessonBlock.Paragraph(
            "Some foods have high calories due to high sugar and fat content. They contain very low " +
                "amounts of proteins, minerals, vitamins, and dietary fibres. These foods are called " +
                "**junk foods** — potato wafers, candy bars, carbonated drinks. Consuming them frequently " +
                "is not good as they are not healthy for our body. They make a person obese.",
        ),

        LessonBlock.Callout(
            tone = Tone.FACT, title = "Packaged food regulation in India",
            body = "Packaged food items must list the amount of each nutrient on the cover. Sometimes, " +
                "more nutrients are added during processing — this is called **fortification** (iodised " +
                "salt and some baby foods are examples). The **Food Safety and Standards Authority of " +
                "India (FSSAI)** is the government agency that regulates food quality in India.",
        ),

        LessonBlock.Heading(number = "3.5", level = 1, text = "Millets: Nutrition-rich Cereals"),
        LessonBlock.Paragraph(
            "You may have heard of **jowar, bajra, ragi, and sanwa**. These are native crops of India. " +
                "They can be easily cultivated in different climatic conditions. These highly nutritious " +
                "grains are also called **millets**. Have you ever had food items made from these millets?",
        ),
        LessonBlock.Paragraph(
            "Millets are small-sized grains that have been an integral part of the Indian diet for " +
                "centuries. They have regained popularity due to their numerous health benefits. They are " +
                "good sources of vitamins, minerals like iron and calcium, and dietary fibres as well. " +
                "That is the reason they are also called **nutri-cereals**. They contribute significantly " +
                "to a balanced diet required for the normal functioning of our body.",
        ),
        LessonBlock.Callout(
            tone = Tone.INFO, title = "What the activity does",
            body = "In the app's comparator: slide the serving size, tap a nutrient row to highlight it " +
                "on both cards. Use this to decide which snack you'd actually pack in your lunchbox.",
        ),
        LessonBlock.Paragraph("Tap **Do** above to open the live comparator."),
    ),
    reflect = listOf(
        LessonBlock.Heading(level = 1, text = "Reading a label is a real skill"),
        LessonBlock.Paragraph(
            "Per 100 g, the wafers carry **6× the fat** and **less than half the protein** of roasted " +
                "chana — and almost no protective dietary fibre. The chana is the better choice on almost " +
                "every dimension.",
        ),
        LessonBlock.Callout(
            tone = Tone.SUCCESS, title = "Two takeaways",
            body = "1. Read the back of the packet, not the front. The marketing on the front is a story; " +
                "the numbers on the back are the truth.\n" +
                "2. Whole, traditional foods (chana, ragi, jowar) routinely beat processed snacks on the " +
                "label.",
        ),
        LessonBlock.CharacterSay(
            who = "Dr Poshita", avatar = Avatar.SCIENTIST, side = Side.LEFT,
            text = "Health is the Ultimate Wealth. Choose food the way you choose a friend — for what " +
                "it actually gives you, not how loud it shouts.",
        ),
    ),
)
