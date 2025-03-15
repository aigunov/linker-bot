package backend.academy.scrapper.service;

import backend.academy.scrapper.repository.tag.TagRepository;
import dto.GetTagsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagsService {
    private final TagRepository tagRepository;

    public GetTagsResponse getUserTags(Long id) {
        return null;
    }
}
