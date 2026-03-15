package and.learn.controller.ai;

import and.learn.ai.englishtextgenerator.EnglishTextGeneratorMain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("englishTextGenerator")
public class EnglishTextGeneratorController {

    // 1. Restituisce la pagina con il form
    @GetMapping(produces = "text/html")
    public String getPage() {
        return """
                <!DOCTYPE html>
                <html>
                <body>
                    <h2>English Text Generator</h2>
                    <form action="/englishTextGenerator/creaStoria" method="GET">
                        <button type="submit">Crea storia</button>
                    </form>
                </body>
                </html>
                """;
    }

    // 2. API chiamata dal pulsante (POST)
    /**
       http://localhost:8080/englishTextGenerator/creaStoria
     * */
    @GetMapping("/creaStoria")
    public String creaStoria() {
        Thread t = new Thread(() -> {
            try {
                EnglishTextGeneratorMain.main(null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        t.start();
        return "Storia in corso di generazione";
    }
}
