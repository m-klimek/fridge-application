package com.infoshareacademy.service;

import com.infoshareacademy.DTO.FridgeDto;
import com.infoshareacademy.DTO.ProductInFridgeDto;
import com.infoshareacademy.entity.fridge.Fridge;
import com.infoshareacademy.entity.product.ProductInFridge;
import com.infoshareacademy.entity.product.ProductUnit;
import com.infoshareacademy.entity.user.User;
import com.infoshareacademy.repository.FridgeRepository;
import com.infoshareacademy.repository.ProductInFridgeRepository;
import com.infoshareacademy.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FridgeService {

    private final FridgeRepository fridgeRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final ProductInFridgeRepository productInFridgeRepository;

    public FridgeService(FridgeRepository fridgeRepository, UserRepository userRepository, ModelMapper modelMapper, ProductInFridgeRepository productInFridgeRepository) {
        this.fridgeRepository = fridgeRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.productInFridgeRepository = productInFridgeRepository;
    }

    public Long getUserId() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetail = (UserDetails) auth.getPrincipal();
        return userRepository.findByUsername(userDetail.getUsername()).map(User::getId).orElse(null);

    }

    @Transactional
    public void saveFridge(FridgeDto fridgeDto) {
        Fridge fridge = modelMapper.map(fridgeDto, Fridge.class);
        fridge.getProductsInFridge().forEach(x -> x.setFridge(fridge));
        fridge.getProductsInFridge().forEach(this::convertUnitsInProducts);
        fridge.setFridgeId(getUserId());
        fridgeRepository.save(fridge);
        log.info("Zapisano lodówkę dla użytkownika o id: " + getUserId());
    }

    public FridgeDto getFridge() {
        FridgeDto fridgeDto;
        boolean flag = fridgeRepository.findById(getUserId()).isPresent();
        if (flag) {
            Fridge fridge = fridgeRepository.findById(getUserId()).get();
            fridgeDto = modelMapper.map(fridge, FridgeDto.class);
        } else {
            fridgeDto = new FridgeDto();
            fridgeDto.setFridgeId(getUserId());
        }
        return fridgeDto;
    }

    public Page<ProductInFridgeDto> getProductsInFridge(Pageable pageable) {
        Page<ProductInFridgeDto> collect = new PageImpl<>(new ArrayList<>());
        boolean flag = productInFridgeRepository.findProductInFridgeByFridge(getUserId(), pageable).isEmpty();
        if (!flag) {
            Page<ProductInFridge> fridgeProducts = productInFridgeRepository.findProductInFridgeByFridge(getUserId(), pageable);
            collect = fridgeProducts.map(productInFridge -> modelMapper.map(productInFridge, ProductInFridgeDto.class));
        }
        return collect;
    }

    public FridgeDto addProductsToFridgeForm() {
        FridgeDto fridgeDto;
        if (fridgeRepository.findById(getUserId()).isPresent()) {
            fridgeDto = fridgeRepository
                    .findById(getUserId())
                    .map((fridge -> modelMapper.map(fridge, FridgeDto.class))).get();
        } else {
            fridgeDto = new FridgeDto();
            fridgeDto.setFridgeId(getUserId());
            fridgeDto.setProductsInFridge(new ArrayList<>());
        }
        return fridgeDto;
    }

    public Map<String, FridgeDto.ProductInFridgeDto> mapProductsInFridgeWithNameAsKey() {
        List<FridgeDto.ProductInFridgeDto> productsInFridgeDto = getFridge().getProductsInFridge();
        if (productsInFridgeDto == null) {
            productsInFridgeDto = new ArrayList<>();
        }
        return productsInFridgeDto
                .stream()
                .peek(productInFridgeDto -> productInFridgeDto
                        .setProductName(productInFridgeDto.getProductName().toLowerCase()))
                .collect(Collectors.toMap(FridgeDto.ProductInFridgeDto::getProductName, Function.identity()));
    }

    public void convertUnitsInProducts(ProductInFridge product) {
        Double convertedAmount = 0.0;
        switch (product.getUnit()) {

            case MILIGRAM:
                convertedAmount = (product.getAmount()) / 1000;
                product.setAmount(convertedAmount);
                product.setUnit(ProductUnit.GRAM);
                break;

            case KILOGRAM:
                convertedAmount = (product.getAmount()) * 1000;
                product.setAmount(convertedAmount);
                product.setUnit(ProductUnit.GRAM);
                break;

            case LITR:
                convertedAmount = (product.getAmount()) * 1000;
                product.setAmount(convertedAmount);
                product.setUnit(ProductUnit.MILILITR);
                break;
        }
    }
}