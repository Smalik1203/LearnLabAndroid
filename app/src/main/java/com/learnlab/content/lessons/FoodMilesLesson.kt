package com.learnlab.content.lessons

import com.learnlab.content.LessonBlock
import com.learnlab.content.LessonBlock.CharacterSay.Avatar
import com.learnlab.content.LessonBlock.CharacterSay.Side
import com.learnlab.content.LessonBlock.Callout.Tone
import com.learnlab.content.LessonContent

/**
 * NCERT Class 6 Science · Chapter 3 · §3.6 (pages 54–55).
 * Food Miles — from farm to plate.
 */
val FoodMilesLesson = LessonContent(
    read = listOf(
        LessonBlock.Heading(number = "3.6", level = 1, text = "Food Miles: From Farm to Our Plate"),
        LessonBlock.Paragraph(
            "How does food reach from a farm to our plate? What are the steps involved in this process? " +
                "Who are the people involved? Do you know how much time and effort is required to get the " +
                "wheat flour once seed grains germinate in the farm?",
        ),
        LessonBlock.Paragraph(
            "Let us look at the **story of chapati** to understand the entire process of making the chapati " +
                "we eat (Fig. 3.10):",
        ),
        LessonBlock.KeyTerm("1. Farmer growing wheat",
            "The seed is sown, watered, weeded for months until the plant matures."),
        LessonBlock.KeyTerm("2. Threshing and winnowing",
            "Grains are separated from the stalks and the chaff is blown away."),
        LessonBlock.KeyTerm("3. Storage of grains",
            "Grains are stored in sacks until they are needed."),
        LessonBlock.KeyTerm("4. Grinding and packing",
            "Grains go to a mill that grinds them into flour, which is then packed."),
        LessonBlock.KeyTerm("5. Transport to retail shop",
            "Trucks move packets from the mill to shops near you."),
        LessonBlock.KeyTerm("6. Food on our plate",
            "Your family buys the flour, kneads it, and rolls it into a chapati you eat."),

        LessonBlock.KeyTerm(
            term = "Food miles",
            definition = "The entire distance travelled by a bag of wheat or any other food item, " +
                "from the producer to the consumer.",
        ),
        LessonBlock.Paragraph(
            "Reducing food miles is important because it helps to **cut down the cost and pollution** " +
                "during its transport; it helps support **local farmers**; and it also keeps our food " +
                "**fresh and healthy**.",
        ),

        LessonBlock.Question(
            prompt = "How would eating local food help reduce food miles?",
        ),

        LessonBlock.Callout(
            tone = Tone.WARNING, title = "Food waste",
            body = "Many people waste food, leaving it unconsumed on their plates. One must remember " +
                "the time and effort put in by our farmers and other community members in getting food " +
                "from the farm to our plate. We must take only as much food as we can consume.",
        ),
        LessonBlock.Paragraph(
            "Tap **Do** above to open the food-miles route planner — pick a destination city, choose a " +
                "food, then choose a source for it. Compare the impact: distance, CO₂, freshness, cost.",
        ),
    ),
    reflect = listOf(
        LessonBlock.Heading(level = 1, text = "Eat healthy, share, and respect food. Support local producers!"),
        LessonBlock.Paragraph(
            "Eating food that is locally grown and plant-based, to the extent possible, is not only " +
                "healthy for the body but is also good for our environment and our planet.",
        ),
        LessonBlock.Callout(
            tone = Tone.SUCCESS, title = "Three things you can do today",
            body = "1. Take only what you'll finish — reduce waste.\n" +
                "2. Pick the local option when you have a choice.\n" +
                "3. Thank the people who grew, harvested, transported, and cooked what's on your plate.",
        ),
        LessonBlock.CharacterSay(
            who = "Medu", avatar = Avatar.STUDENT_BOY, side = Side.RIGHT,
            text = "I never realised one chapati was the work of so many people. I'll think twice before " +
                "I leave food on my plate.",
        ),
    ),
)
