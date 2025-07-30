package com.carava.carwash.member.entity

import com.carava.carwash.shared.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "customer_member")
data class CustomerMember(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "auth_id", nullable = false, unique = true)
    var authId: Long,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false, length = 20)
    var phone: String,

    @Column(name = "birth_date")
    var birthDate: LocalDate? = null,

    @Column(name = "address_id")
    var addressId: Long? = null,

    @Column(name = "profile_image_url")
    var profileImageUrl: String? = null,

    @Column(length = 50)
    var nickname: String? = null

) : BaseEntity() {

    fun updateProfile(
        name: String? = null,
        phone: String? = null,
        nickname: String? = null,
        birthDate: LocalDate? = null
    ) {
        name?.let { this.name = it }
        phone?.let { this.phone = it }
        nickname?.let { this.nickname = it }
        birthDate?.let { this.birthDate = it }
    }

    fun updateAddress(addressId: Long?) {
        this.addressId = addressId
    }

    fun updateProfileImage(imageUrl: String?) {
        this.profileImageUrl = imageUrl
    }
} 