package and.learn.controller.ai;

import and.learn.ai.englishtextgenerator.EnglishTextGeneratorMain;
import and.learn.ai.englishtextgenerator.EnglishTextGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("englishTextGenerator")
public class EnglishTextGeneratorController {

    @Autowired
    private EnglishTextGeneratorService englishTextGeneratorService;

    // 1. Restituisce la pagina con il form
    @GetMapping(produces = "text/html")
    public String getPage() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>English Text Generator</title>
                </head>
                <body>
                    <h2>English Text Generator</h2>
                    <form id="btnCrea" action="/englishTextGenerator/creaStoria" method="GET" onsubmit="mostraMessaggio()">
                        <button type="submit">Crea storia</button>
                    </form>
                    
                    <p id="messaggioStato" style="margin-top: 15px; color: blue; font-weight: bold; font-family: Arial, sans-serif;"></p>
                    
                    <script>
                            function mostraMessaggio() {
                                // Inserisce il testo nel paragrafo
                                document.getElementById("messaggioStato").innerText = "Generazione storia avviata...";
                
                                // Opzionale: disabilita il pulsante per evitare doppi click
                                document.getElementById("btnCrea").disabled = true;
                            }
                        </script>
                </body>
                </html>
                """;
    }

    // 2. API chiamata dal pulsante (POST)
    /**
       http://localhost:8080/englishTextGenerator/creaStoria
     * */
    @GetMapping("/creaStoria")
    public String creaStoria() throws Exception {

        englishTextGeneratorService.generaStoria();
        return "Storia generata con successo! Controlla su Google Drive per vedere il risultato.";
    }
}
