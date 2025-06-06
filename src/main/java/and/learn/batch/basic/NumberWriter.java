package and.learn.batch.basic;

import lombok.extern.log4j.Log4j2;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

@Log4j2
public class NumberWriter implements ItemWriter<Integer> {
    @Override
    public void write(Chunk<? extends Integer> chunk) {
        log.debug("Writing chunk: " + chunk.getItems());
    }
}