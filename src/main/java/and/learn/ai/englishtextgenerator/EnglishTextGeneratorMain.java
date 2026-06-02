package and.learn.ai.englishtextgenerator;

import and.learn.ai.englishtextgenerator.gemini.behaviour.ChatGeminiBehaviour;
import and.learn.ai.englishtextgenerator.gemini.behaviour.ChatGeminiBehaviourCreator;
import and.learn.ai.englishtextgenerator.gemini.behaviour.abstraction.ChatGeminiAbstract;
import and.learn.ai.englishtextgenerator.googleservice.DocsManager;
import and.learn.ai.englishtextgenerator.googleservice.DriveManager;
import and.learn.ai.englishtextgenerator.googleservice.GoogleServicesFactory;
import io.micrometer.common.util.StringUtils;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class EnglishTextGeneratorMain {

    public static void main(String[] args) throws Exception {
        EnglishTextGeneratorService service = new EnglishTextGeneratorService();
        service.generaStoria();
    }
}
