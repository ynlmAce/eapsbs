package com.bs.eaps.service.impl;

import com.bs.eaps.entity.CompanyLicenseFile;
import com.bs.eaps.entity.CompanyProfile;
import com.bs.eaps.mapper.CompanyLicenseFileMapper;
import com.bs.eaps.mapper.CompanyProfileMapper;
import com.bs.eaps.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 企业服务实现类
 */
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyProfileMapper companyProfileMapper;
    private final CompanyLicenseFileMapper companyLicenseFileMapper;

    @Value("${upload.path:/uploads}")
    private String uploadPath;

    @Override
    public CompanyProfile getCompanyProfile(Long companyId) {
        return companyProfileMapper.selectById(companyId);
    }

    @Override
    @Transactional
    public boolean updateCompanyProfile(CompanyProfile profile) {
        return companyProfileMapper.updateById(profile) > 0;
    }

    @Override
    @Transactional
    public CompanyLicenseFile uploadCompanyLicense(Long companyId, MultipartFile file) {
        // 创建上传目录
        Path directoryPath = Paths.get(uploadPath, "company", "license");
        try {
            Files.createDirectories(directoryPath);

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = directoryPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);

            // 保存文件记录
            CompanyLicenseFile licenseFile = new CompanyLicenseFile();
            licenseFile.setCompanyProfileId(companyId);
            licenseFile.setFileName(originalFilename);
            licenseFile.setFilePath("/uploads/company/license/" + newFilename);

            companyLicenseFileMapper.insert(licenseFile);

            return licenseFile;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }
}