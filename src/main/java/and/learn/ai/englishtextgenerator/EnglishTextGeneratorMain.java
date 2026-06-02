package and.learn.ai.englishtextgenerator;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class EnglishTextGeneratorMain {

    public static void main(String[] args) throws Exception {
        EnglishTextGeneratorService service = new EnglishTextGeneratorService();
        service.generaStoria();
    }
}
