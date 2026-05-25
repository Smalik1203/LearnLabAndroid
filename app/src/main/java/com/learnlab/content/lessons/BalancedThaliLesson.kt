package com.learnlab.content.lessons

import com.learnlab.content.LessonBlock
import com.learnlab.content.LessonBlock.CharacterSay.Avatar
import com.learnlab.content.LessonBlock.CharacterSay.Side
import com.learnlab.content.LessonBlock.Callout.Tone
import com.learnlab.content.LessonContent

/**
 * NCERT Class 6 Science · Chapter 3 · §3.4 · Activity 3.8 (page 51).
 * Balanced Diet — composing a plate that hits all nutrient groups.
 */
val BalancedThaliLesson = LessonContent(
    read = listOf(
        LessonBlock.Heading(number = "3.4", level = 1, text = "Balanced Diet"),
        LessonBlock.Paragraph(
            "Are nutritional requirements the same for everyone? Do you and your grandparents need " +
                "the same type or the same amount of nutrients?",
        ),
        LessonBlock.Paragraph(
            "Requirements of the type and amount of nutrients in a diet may vary according to **age, " +
                "gender, physical activity, health status, lifestyle**, and so on.",
        ),

        LessonBlock.KeyTerm(
            term = "Balanced diet",
            definition = "A diet that has all essential nutrients, roughage and water in the right amount " +
                "for proper growth and development of the body.",
        ),

        LessonBlock.Heading(level = 2, text = "Activity 3.8: Let us find out"),
        LessonBlock.Paragraph(
            "Think of the food you ate over the past week. Does it contain all the nutrients and " +
                "other essential components necessary for growth and development? If not, which nutrients " +
                "or food components need to be added? What changes would you make in your diet?",
        ),

        LessonBlock.CharacterSay(
            who = "Dr Poshita", avatar = Avatar.SCIENTIST, side = Side.LEFT,
            text = "A balanced thali is not about more food. It is about the right mix — carbs " +
                "for energy, protein to build, fat to store, vitamins and minerals to protect.",
        ),

        LessonBlock.Callout(
            tone = Tone.INFO, title = "The five-target rule",
            body = "In the app version, the plate has FIVE nutrient bars on the right: carbohydrates, " +
                "protein, fat, vitamins, minerals. Drag dishes onto the plate until every bar crosses " +
                "its target line. That is a balanced thali.",
        ),

        LessonBlock.Paragraph(
            "Some dishes are good at one thing only (ghee → almost pure fat). Others contribute a " +
                "little to many bars (dal → protein + some carbs + minerals). A balanced thali usually " +
                "needs **at least one item from every category**.",
        ),
        LessonBlock.Question(
            prompt = "Which dishes in the pantry will push the vitamins bar the most? Hint: think " +
                "spinach, carrot, amla.",
        ),
        LessonBlock.Paragraph("Tap **Do** above and try composing one."),
    ),
    reflect = listOf(
        LessonBlock.Heading(level = 1, text = "What balance actually means"),
        LessonBlock.Paragraph(
            "If you piled the plate with just rice and ghee, the energy bars hit the target — but the " +
                "vitamin and mineral bars stayed empty. That's not balance, that's surplus.",
        ),
        LessonBlock.Paragraph(
            "A balanced diet provides all the essential nutrients in the right quantities, along with " +
                "adequate **roughage** and water. Deficiency of one or more nutrients in your diet for a " +
                "long time can lead to deficiency diseases and disorders.",
        ),
        LessonBlock.Callout(
            tone = Tone.SUCCESS, title = "The textbook's bottom line",
            body = "Health is the Ultimate Wealth. Eating a balanced diet and avoiding junk food " +
                "contribute towards a healthy body. Good health is essential for leading a happy life.",
        ),
        LessonBlock.KeyTerm(
            term = "Roughage",
            definition = "Also known as dietary fibre — does not provide any nutrients, but helps the body " +
                "get rid of undigested food. Found in green leafy vegetables, fresh fruits, wholegrains, " +
                "pulses, nuts.",
        ),
    ),
)
