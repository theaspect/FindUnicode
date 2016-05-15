import org.junit.Test

import static org.junit.Assert.assertEquals

class FindTest {
    @Test
    void clearLine(){
        Find find = new Find()
        InputStream file = new ByteArrayInputStream("This is good".getBytes())
        assertEquals(find.analysisFile(file).isEmpty(), true)
    }

    @Test
    void lessFiveChar(){
        Find find = new Find()
        InputStream file = new ByteArrayInputStream("This is хopoшo".getBytes())
        assertEquals(find.analysisFile(file).first().message, " - 2 not ASCII of characters: ")
    }

    @Test
    void moreFiveChar(){
        Find find = new Find()
        InputStream file = new ByteArrayInputStream("Тhis is хорошо".getBytes())
        assertEquals(find.analysisFile(file).first().message, " more than 5 not ASCII of characters: ")
    }

    @Test
    void withBomChar(){
        Find find = new Find()
        InputStream file = new ByteArrayInputStream(("\uFEFF" + "This is хopoшo").getBytes())
        assertEquals(find.analysisFile(file).first().message, " - 2 not ASCII of characters: ")
    }
}
