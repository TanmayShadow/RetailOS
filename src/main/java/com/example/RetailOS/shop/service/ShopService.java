package com.example.RetailOS.shop.service;

import com.example.RetailOS.platform.exception.ResourceAlreadyExistsException;
import com.example.RetailOS.platform.exception.ResourceNotFoundException;
import com.example.RetailOS.platform.security.service.CurrentUserService;
import com.example.RetailOS.shop.dto.request.CreateShopRequest;
import com.example.RetailOS.shop.dto.request.UpdateShopRequest;
import com.example.RetailOS.shop.dto.response.ShopResponse;
import com.example.RetailOS.shop.entity.Shop;
import com.example.RetailOS.shop.repository.ShopRepository;
import com.example.RetailOS.user.entity.User;
import com.example.RetailOS.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public ShopResponse createShop(CreateShopRequest request) {

        User user = userRepository.findById(currentUserService.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Registration assigns a placeholder shopId; a real shop existing
        // under it means this account already completed shop setup.
        if (shopRepository.existsById(user.getShopId())) {
            throw new ResourceAlreadyExistsException("A shop is already registered for this account.");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (shopRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("A shop with this email already exists.");
        }

        Shop shop = Shop.builder()
                .name(request.getName().trim())
                .slug(generateUniqueSlug(request.getName()))
                .phone(request.getPhone().trim())
                .email(email)
                .gstIn(request.getGstIn())
                .logoUrl(request.getLogoUrl())
                .invoicePrefix(request.getInvoicePrefix() == null
                        ? "INV" : request.getInvoicePrefix().trim())
                .invoiceFooter(request.getInvoiceFooter())
                .whatsappNumber(request.getWhatsappNumber())
                .upiId(request.getUpiId())
                .build();

        shop = shopRepository.save(shop);

        user.setShopId(shop.getId());
        userRepository.save(user);

        return ShopResponse.from(shop);
    }

    public ShopResponse getMyShop() {
        return ShopResponse.from(requireCurrentShop());
    }

    @Transactional
    public ShopResponse updateMyShop(UpdateShopRequest request) {

        Shop shop = requireCurrentShop();

        if (request.getName() != null) {
            shop.setName(request.getName().trim());
        }
        if (request.getPhone() != null) {
            shop.setPhone(request.getPhone().trim());
        }
        if (request.getGstIn() != null) {
            shop.setGstIn(request.getGstIn().trim());
        }
        if (request.getLogoUrl() != null) {
            shop.setLogoUrl(request.getLogoUrl().trim());
        }
        if (request.getInvoicePrefix() != null) {
            shop.setInvoicePrefix(request.getInvoicePrefix().trim());
        }
        if (request.getInvoiceFooter() != null) {
            shop.setInvoiceFooter(request.getInvoiceFooter());
        }
        if (request.getWhatsappNumber() != null) {
            shop.setWhatsappNumber(request.getWhatsappNumber().trim());
        }
        if (request.getUpiId() != null) {
            shop.setUpiId(request.getUpiId().trim());
        }

        return ShopResponse.from(shopRepository.save(shop));
    }

    public ShopResponse getShopBySlug(String slug) {
        Shop shop = shopRepository.findBySlug(slug.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found: " + slug));
        return ShopResponse.from(shop);
    }

    public boolean isSlugAvailable(String slug) {
        return !shopRepository.existsBySlug(slugify(slug));
    }

    private Shop requireCurrentShop() {
        return shopRepository.findById(currentUserService.getCurrentShopId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No shop registered for this account."));
    }

    private String generateUniqueSlug(String name) {
        String base = slugify(name);
        String slug = base;
        int suffix = 2;
        while (shopRepository.existsBySlug(slug)) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }

    private String slugify(String input) {
        String slug = input.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+)|(-+$)", "");
        return slug.length() > 90 ? slug.substring(0, 90) : slug;
    }
}
