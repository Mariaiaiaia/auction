package com.maria.handler;

import com.maria.constant.ItemServiceConstants;
import com.maria.entity.CreateItemRequest;
import com.maria.service.ItemService;
import com.maria.validator.ItemServiceValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ItemHandler {
    private final ItemService itemService;
    private final ItemServiceValidation itemServiceValidation;

    public Mono<ServerResponse> getSellerId(ServerRequest request) {
        return itemService.getSellerId(Long.valueOf(request.pathVariable("id")))
                .flatMap(sellerId -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(sellerId));
    }

    public Mono<ServerResponse> getItem(ServerRequest request) {
        return itemServiceValidation.validateItemId(request.pathVariable("id"))
                .flatMap(itemService::getItem)
                .flatMap(itemDTO -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(itemDTO));
    }

    public Mono<ServerResponse> createItem(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(auth -> Long.valueOf(auth.getAuthentication().getPrincipal().toString()))
                .flatMap(userId -> request.multipartData()
                        .flatMap(parts -> {
                            FilePart filePart = (FilePart) parts.getFirst("file");
                            FormFieldPart itemNamePart = (FormFieldPart) parts.getFirst("itemName");
                            FormFieldPart itemDescriptionPart = (FormFieldPart) parts.getFirst("itemDescription");

                            assert itemNamePart != null;
                            assert itemDescriptionPart != null;
                            CreateItemRequest item = CreateItemRequest.builder()
                                    .itemName(itemNamePart.value())
                                    .description(itemDescriptionPart.value())
                                    .build();
                            if (filePart == null) {
                                return Mono.error(new IllegalArgumentException(ItemServiceConstants.FILE_MISSING));
                            }

                            return filePart.content()
                                    .map(dataBuffer -> {
                                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                        dataBuffer.read(bytes);
                                        DataBufferUtils.release(dataBuffer);
                                        return bytes;
                                    })
                                    .reduce((bytes1, bytes2) -> {
                                        byte[] combined = new byte[bytes1.length + bytes2.length];
                                        System.arraycopy(bytes1, 0, combined, 0, bytes1.length);
                                        System.arraycopy(bytes2, 0, combined, bytes1.length, bytes2.length);
                                        return combined;
                                    })
                                    .flatMap(fileBytes -> itemService.createItem(item, userId, fileBytes));
                        }))
                .flatMap(itemDTO ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(itemDTO));
    }
}


