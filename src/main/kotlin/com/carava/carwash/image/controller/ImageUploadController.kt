package com.carava.carwash.image.controller

import com.carava.carwash.global.annotation.CurrentMemberId
import com.carava.carwash.global.dto.ApiResponse
import com.carava.carwash.image.dto.ImageListResponseDto
import com.carava.carwash.image.dto.ImageUploadResponseDto
import com.carava.carwash.image.entity.ImageCategory
import com.carava.carwash.image.service.ImageUploadService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/images")
@Tag(name = "이미지 업로드", description = "이미지 업로드 및 관리 API")
class ImageUploadController(
    private val imageUploadService: ImageUploadService,
    private val jwtUtil: JwtUtil
) {
    
    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "이미지 업로드",
        description = "이미지를 임시 위치에 업로드합니다. 나중에 예약이나 다른 엔티티와 연결할 수 있습니다."
    )
    @ApiResponses(
        io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "업로드 성공"),
        io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    )
    fun uploadImage(
        @Parameter(description = "업로드할 이미지 파일", required = true)
        @RequestParam("file") file: MultipartFile,
        
        @Parameter(description = "이미지 카테고리", required = true)
        @RequestParam("category") category: ImageCategory,
        
        @Parameter(description = "이미지 설명")
        @RequestParam("description", required = false) description: String?,
        
        @CurrentMemberId customerId: Long
    ): ResponseEntity<ApiResponse<ImageUploadResponseDto>> {

        
        val result = imageUploadService.uploadImage(file, customerId, category, description)
        
        return if (result.success) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result)
        }
    }
    
    @PostMapping("/{imageId}/link")
    @Operation(
        summary = "이미지를 엔티티와 연결",
        description = "업로드된 임시 이미지를 특정 엔티티(예: 예약)와 연결하고 정식 위치로 이동합니다."
    )
    @ApiResponses(
        io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "연결 성공"),
        io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이미지 없음")
    )
    fun linkImageToEntity(
        @Parameter(description = "이미지 ID", required = true)
        @PathVariable imageId: Long,
        
        @Parameter(description = "연결할 엔티티 타입", required = true)
        @RequestParam("entityType") entityType: String,
        
        @Parameter(description = "연결할 엔티티 ID", required = true)
        @RequestParam("entityId") entityId: Long,
        
        @CurrentMemberId customerId: Long
    ): ResponseEntity<ApiResponse<ImageUploadResponseDto>> {

        
        val result = imageUploadService.linkImageToEntity(imageId, customerId, entityType, entityId)
        
        return when {
            result.success -> ResponseEntity.ok(result)
            result.message.contains("찾을 수 없습니다") -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(result)
            result.message.contains("권한이 없습니다") -> ResponseEntity.status(HttpStatus.FORBIDDEN).body(result)
            else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result)
        }
    }
    
    @GetMapping
    @Operation(
        summary = "내 이미지 목록 조회",
        description = "사용자가 업로드한 이미지 목록을 조회합니다. 카테고리, 엔티티 타입/ID로 필터링 가능합니다."
    )
    @ApiResponses(
        io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    )
    fun getMyImages(
        @Parameter(description = "이미지 카테고리 필터")
        @RequestParam("category", required = false) category: ImageCategory?,
        
        @Parameter(description = "엔티티 타입 필터")
        @RequestParam("entityType", required = false) entityType: String?,
        
        @Parameter(description = "엔티티 ID 필터")
        @RequestParam("entityId", required = false) entityId: Long?,
        
        @PageableDefault(size = 20) pageable: Pageable,
        @CurrentMemberId customerId: Long
    ): ResponseEntity<ApiResponse<Page<ImageListResponseDto>>> {

        
        val result = imageUploadService.getUserImages(customerId, category, entityType, entityId, pageable)
        
        return if (result.success) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result)
        }
    }
    
    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(
        summary = "특정 엔티티의 이미지 조회",
        description = "특정 엔티티(예: 예약 ID 123)에 연결된 이미지들을 조회합니다."
    )
    @ApiResponses(
        io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    )
    fun getEntityImages(
        @Parameter(description = "엔티티 타입", required = true)
        @PathVariable entityType: String,
        
        @Parameter(description = "엔티티 ID", required = true)
        @PathVariable entityId: Long,
        
        @Parameter(description = "이미지 카테고리 필터")
        @RequestParam("category", required = false) category: ImageCategory?,
        
        @PageableDefault(size = 20) pageable: Pageable,
        @CurrentMemberId customerId: Long
    ): ResponseEntity<ApiResponse<Page<ImageListResponseDto>>> {

        
        val result = imageUploadService.getUserImages(customerId, category, entityType, entityId, pageable)
        
        return if (result.success) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result)
        }
    }
    
    @DeleteMapping("/{imageId}")
    @Operation(
        summary = "이미지 삭제",
        description = "업로드한 이미지를 삭제합니다. 연결된 엔티티가 있어도 삭제됩니다."
    )
    @ApiResponses(
        io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
        io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이미지 없음")
    )
    fun deleteImage(
        @Parameter(description = "삭제할 이미지 ID", required = true)
        @PathVariable imageId: Long,
        
        @CurrentMemberId customerId: Long
    ): ResponseEntity<ApiResponse<Unit>> {

        
        val result = imageUploadService.deleteImage(imageId, customerId)
        
        return when {
            result.success -> ResponseEntity.ok(result)
            result.message.contains("찾을 수 없습니다") -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(result)
            result.message.contains("권한이 없습니다") -> ResponseEntity.status(HttpStatus.FORBIDDEN).body(result)
            else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result)
        }
    }
    
    @GetMapping("/temporary")
    @Operation(
        summary = "임시 이미지 목록 조회",
        description = "아직 엔티티와 연결되지 않은 임시 이미지들을 조회합니다."
    )
    @ApiResponses(
        io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    )
    fun getTemporaryImages(
        @Parameter(description = "이미지 카테고리 필터")
        @RequestParam("category", required = false) category: ImageCategory?,
        
        @PageableDefault(size = 20) pageable: Pageable,
        @CurrentMemberId customerId: Long
    ): ResponseEntity<ApiResponse<Page<ImageListResponseDto>>> {

        
        // entityType과 entityId가 null인 경우 = 임시 이미지
        val result = imageUploadService.getUserImages(customerId, category, null, null, pageable)
        
        return if (result.success) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result)
        }
    }
}