package and.learn.batch.basic;

import lombok.extern.log4j.Log4j2;
import org.springframework.batch.item.ItemProcessor;

@Log4j2
public class NumberProcessor implements ItemProcessor<Integer, Integer> {
    @Override
    public Integer process(Integer number) {
        log.debug("Processing number: " + number);
        return number * 2;
    }
}