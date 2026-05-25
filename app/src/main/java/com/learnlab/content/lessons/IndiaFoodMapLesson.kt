package com.learnlab.content.lessons

import com.learnlab.content.LessonBlock
import com.learnlab.content.LessonBlock.CharacterSay.Avatar
import com.learnlab.content.LessonBlock.CharacterSay.Side
import com.learnlab.content.LessonBlock.Callout.Tone
import com.learnlab.content.LessonContent

/**
 * NCERT Class 6 Science · Chapter 3 · §3.1 + §3.1.1 · Activity 3.2
 * (Pages 35–37) — Food in different regions of India.
 */
val IndiaFoodMapLesson = LessonContent(
    read = listOf(
        LessonBlock.Quote(
            original = "अन्नेन जातानि जीवन्ति",
            text = "Annena jātāni jīvanti — food gives life to living beings.",
            attribution = "Taittirīya Upaniṣada",
        ),
        LessonBlock.CharacterSay(
            who = "Mishti", avatar = Avatar.STUDENT_GIRL, side = Side.LEFT,
            text = "Today's thought on the noticeboard says food gives life. Let's see what that really means.",
        ),
        LessonBlock.CharacterSay(
            who = "Medu", avatar = Avatar.STUDENT_BOY, side = Side.RIGHT,
            text = "We all eat food every day. But do we eat the same things across India?",
        ),

        LessonBlock.Heading(number = "3.1", level = 1, text = "What Do We Eat?"),
        LessonBlock.Paragraph(
            "All of us eat food every day. Food is an essential component of our daily life. " +
                "If you list everything you ate this week, you'll notice the variety changes from " +
                "one meal to the next — even within one family.",
        ),
        LessonBlock.Paragraph(
            "Do your friends eat the same things you do? Probably not exactly. " +
                "There are similarities and differences. That's the first observation.",
        ),

        LessonBlock.Heading(number = "3.1.1", level = 2, text = "Food in different regions"),
        LessonBlock.Paragraph(
            "Do you think the diversity in food exists in all states of our country? Yes — and " +
                "the food you grow up eating depends a lot on **where you live**.",
        ),

        LessonBlock.Callout(
            tone = Tone.INFO,
            title = "The big rule",
            body = "The traditional food of any state is usually based on the crops grown in that state. " +
                "India is a vast agricultural country with diverse soils and climates — so the food varies.",
        ),

        LessonBlock.Paragraph(
            "Here are a few examples straight from the textbook (Table 3.2):",
        ),
        LessonBlock.KeyTerm(
            term = "Punjab",
            definition = "Wheat, maize, chickpea, pulses → makki di roti, sarson da saag, chhole bhature, " +
                "parantha, lassi.",
        ),
        LessonBlock.KeyTerm(
            term = "Karnataka",
            definition = "Rice, ragi, urad, coconut → idli, dosa, sambhar, ragi mudde, rasam, coconut chutney.",
        ),
        LessonBlock.KeyTerm(
            term = "Manipur",
            definition = "Rice, bamboo, soya bean → rice with eromba chutney, utti (yellow peas + green " +
                "onion curry), singju, kangsoi.",
        ),
        LessonBlock.KeyTerm(
            term = "Kerala",
            definition = "Coconut, rice, fish, spices → appam, meen curry, payasam.",
        ),

        LessonBlock.Question(
            prompt = "Why do you think a Punjabi family eats wheat-based food daily while a " +
                "family in Karnataka eats more rice and ragi?",
        ),
        LessonBlock.Paragraph(
            "The activity tests exactly this — match each traditional dish to the state it " +
                "comes from. Tap **Do** above to start. The hint under each state lists the " +
                "staples grown there. Use those to reason your way through.",
        ),
    ),
    reflect = listOf(
        LessonBlock.Heading(level = 1, text = "What this tells us"),
        LessonBlock.Paragraph(
            "Some food items are common across states — rice, dal, roti — while others are " +
                "specific to one region. The pattern matches the crops grown locally.",
        ),
        LessonBlock.Paragraph(
            "In various regions of India, the choice of food may vary according to the **cultivation** " +
                "of food crops in that particular region, taste preferences, culture, and traditions.",
        ),
        LessonBlock.Callout(
            tone = Tone.SUCCESS, title = "Takeaway",
            body = "Food = climate + soil + history. The map of Indian cuisine is really a map of " +
                "Indian agriculture and culture combined.",
        ),
        LessonBlock.CharacterSay(
            who = "Medu", avatar = Avatar.STUDENT_BOY, side = Side.LEFT,
            text = "So if I move to Kerala, I should expect rice, coconut and fish — not parantha!",
        ),
        LessonBlock.CharacterSay(
            who = "Mishti", avatar = Avatar.STUDENT_GIRL, side = Side.RIGHT,
            text = "And the food keeps you in tune with the season and the place. That's what " +
                "the Sanskrit shloka at the top meant.",
        ),
    ),
)
