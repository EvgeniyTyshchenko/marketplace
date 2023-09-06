package ru.evgeniy.marketplace.controller.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.evgeniy.marketplace.controller.AdApiController;
import ru.evgeniy.marketplace.dto.*;
import ru.evgeniy.marketplace.entity.Ad;
import ru.evgeniy.marketplace.service.AdService;
import ru.evgeniy.marketplace.service.UserService;

@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequestMapping("/ads")
public class AdController implements AdApiController {

    private final AdService adService;
    private final UserService userService;

    @Autowired
    public AdController(AdService adService, UserService userService) {
        this.adService = adService;
        this.userService = userService;
    }

    @GetMapping()
    public AdsDTO getAllAds() {
        return adService.getAllAds();
    }

    @PostMapping()
    public ResponseEntity<AdDTO> addAd(@RequestPart("properties") AdUpdaterDTO adUpdaterDTO,
                                       @RequestPart("image") MultipartFile file,
                                       Authentication authentication) {
        AdDTO adDTO = adService.addAd(adUpdaterDTO, file, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(adDTO);
    }

    @GetMapping("/{id}")
    public ExtendedAdDTO getAd(@PathVariable("id") long id) {
        Ad ad = adService.getAd(id);
        return ExtendedAdDTO.toDto(ad);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAd(@PathVariable("id") long id,
                                      Authentication authentication) {
        adService.removeAd(id, authentication);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}")
    public AdDTO updateAd(@PathVariable("id") long id,
                          @RequestBody AdUpdaterDTO adUpdaterDTO,
                          Authentication authentication) {
        return adService.updateAd(id, adUpdaterDTO, authentication);
    }

    @GetMapping("/me")
    public AdsDTO getAllUserAds(Authentication authentication) {
        return userService.getUserAds(authentication);
    }

    @PatchMapping("/{id}/image")
    public ResponseEntity<byte[]> updateAdImage(@PathVariable("id") long id,
                                                @RequestParam("image") MultipartFile file,
                                                Authentication authentication) {
        ImageDTO image = adService.updateAdImage(id, file, authentication);
        byte[] bytes = image.bytes;
        return ResponseEntity.ok()
                .contentLength(bytes.length)
                .contentType(image.mediaType)
                .body(bytes);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getAdImage(@PathVariable("id") long id) {
        ImageDTO image = adService.getAdImage(id);
        byte[] bytes = image.bytes;
        return ResponseEntity.ok()
                .contentLength(bytes.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }

    @GetMapping("/{id}/comments")
    public CommentsDTO getAdComments(@PathVariable("id") long id) {
        return adService.getAdComments(id);
    }

    @PostMapping("/{id}/comments")
    public CommentDTO addComment(@PathVariable("id") long id,
                                 @RequestBody CommentUpdaterDTO commentUpdaterDTO) {
        return adService.addCommentToAd(id, commentUpdaterDTO);
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable("id") long id,
                                           @PathVariable("commentId") long commentId,
                                           Authentication authentication) {
        adService.removeComment(id, commentId, authentication);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/comments/{commentId}")
    public CommentDTO updateComment(@PathVariable("id") long id,
                                    @PathVariable("commentId") long commentId,
                                    @RequestBody CommentUpdaterDTO commentUpdaterDTO,
                                    Authentication authentication) {
        return adService.updateComment(id, commentId, commentUpdaterDTO, authentication);
    }
}