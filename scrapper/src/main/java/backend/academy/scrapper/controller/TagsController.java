package backend.academy.scrapper.controller;

import backend.academy.scrapper.service.TagService;
import dto.GetTagsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/tags")
public class TagsController {
    private final TagService tagsService;

    @GetMapping
    public ResponseEntity<GetTagsResponse> getAllTags(@RequestHeader("Tg-Chat-Id") Long id) {
        log.info("Get all tags for chat  ID: {}", id.toString().replaceAll("[\r\n]", ""));
        var response = tagsService.getUserTags(id);
        return ResponseEntity.ok(response);
    }
}
