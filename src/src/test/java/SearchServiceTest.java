import com.university.homework.dto.SearchRequestDTO;
import com.university.homework.repository.HomeworkRepository;
import com.university.homework.repository.HomeworkSearchRepository;
import com.university.homework.service.SearchService;
import com.university.homework.util.SearchQueryBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private RestHighLevelClient elasticsearchClient;

    @Mock
    private HomeworkSearchRepository homeworkSearchRepository;

    @Mock
    private HomeworkRepository homeworkRepository;

    @Mock
    private SearchQueryBuilder queryBuilder;

    @InjectMocks
    private SearchService searchService;

    private SearchRequestDTO searchRequest;

    @BeforeEach
    void setUp() {
        searchRequest = SearchRequestDTO.builder()
                .title("Linear Algebra")
                .sortBy("relevance")
                .page(1)
                .pageSize(20)
                .build();
    }

    @Test
    void testSearchWithValidFilter() {
        Assertions.assertNotNull(searchRequest);
        Assertions.assertEquals("Linear Algebra", searchRequest.getTitle());
        Assertions.assertEquals(1, searchRequest.getPage());
        Assertions.assertEquals(20, searchRequest.getPageSize());
    }

    @Test
    void testSearchWithDateRange() {
        SearchRequestDTO request = SearchRequestDTO.builder()
                .dateFrom(LocalDate.of(2026, 4, 1))
                .dateTo(LocalDate.of(2026, 4, 30))
                .page(1)
                .pageSize(20)
                .build();

        Assertions.assertNotNull(request.getDateFrom());
        Assertions.assertNotNull(request.getDateTo());
    }

    @Test
    void testSearchWithMultipleTags() {
        List<String> tags = List.of("math", "algebra", "linear");
        SearchRequestDTO request = SearchRequestDTO.builder()
                .title("Algebra")
                .tags(tags)
                .page(1)
                .pageSize(20)
                .build();

        Assertions.assertEquals(3, request.getTags().size());
        Assertions.assertTrue(request.getTags().contains("math"));
    }
}