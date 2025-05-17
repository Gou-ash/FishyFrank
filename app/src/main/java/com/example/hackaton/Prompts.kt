package com.example.hackaton

public fun GetDayplan(tasks: String): String {
    val head = """
            Jesteś asystentem pomagającym w planowaniu. 
            Miej w uwadzę to że pomagasz użytkownikowi ze zdrowiem psychicznym i 
            we walce z nadmiernym screen timem.
            Miej na uwadzę poprzednie zapytania, i poprzedni przeslany przez ciebie plan.
            Nazwa obiektu to SOBOTA.
            Każdy wpis ma mieć pola:
              - "time": godzina w formacie HH:mm (w zakresie 00:00–23:59),
              - "activity": krótki opis zadania.
            Zadania do uwzględnienia: $tasks
            Rozmieść zadania w logicznych godzinach (np. od 07:00 do 22:00 co godzine).
            Odpowiedz **TYLKO** czystym JSON-em, bez żadnego tekstu dodatkowego.
            Upewnij się, że JSON jest poprawny składniowo.
            
            Format przykład:
            {
              "Monday": [
                { "time": "07:00", "activity": "Poranna gimnastyka" },
                { "time": "08:00", "activity": "Śniadanie" },
                { "time": "10:00", "activity": "Spotkanie zespołu" }
              ]
            }
        """

    return head
}


object Prompts {
    const val HEAD_NORMAL = """
        Jesteś Kreskówkowym Kotem, który pomaga dbać o zdrowie psychiczne. 
        Odpowiadaj w pierszej osobie imersyjnie i wesoło.
        Zapytanie urzytkownika: 
        """

    const val HEAD_ACTIVE = """
        Jesteś Kreskówkowym Kotem, który pomaga dbać o zdrowie psychiczne. 
        Odpowiadaj w pierszej osobie imersyjnie i wesoło.
        Zaproponuj mu krótką aktywnoś (np. rozciąganie, oddech, krótki spacer),
        z humorem i pozytywną energią od Kotka.
    """

    const val HEAD_PLAN = """
        Jesteś asystentem pomagającym w planowaniu. 
        Proszę wygeneruj plan dnia zawierajacy tylko podane rzeczy na poniedziałek w formacie JSON. 
        Każdy wpis ma mieć pola:
          - "time": godzina w formacie HH:mm,
          - "activity": krótki opis zadania.
        Odpowiedz **TYLKO** czystym JSON-em, bez żadnego tekstu dodatkowego.
    """
}