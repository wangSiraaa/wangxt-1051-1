package com.river.sand.service;

import com.river.sand.entity.RiverSection;
import com.river.sand.entity.User;
import com.river.sand.entity.Vessel;
import com.river.sand.enums.Role;
import com.river.sand.repository.RiverSectionRepository;
import com.river.sand.repository.UserRepository;
import com.river.sand.repository.VesselRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MasterDataService {

    private final UserRepository userRepository;
    private final RiverSectionRepository riverSectionRepository;
    private final VesselRepository vesselRepository;

    public MasterDataService(UserRepository userRepository,
                             RiverSectionRepository riverSectionRepository,
                             VesselRepository vesselRepository) {
        this.userRepository = userRepository;
        this.riverSectionRepository = riverSectionRepository;
        this.vesselRepository = vesselRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public List<RiverSection> getAllRiverSections() {
        return riverSectionRepository.findByEnabled(true);
    }

    public RiverSection getRiverSectionById(Long id) {
        return riverSectionRepository.findById(id).orElse(null);
    }

    public List<Vessel> getAllVessels() {
        return vesselRepository.findByEnabled(true);
    }

    public List<Vessel> getVesselsByOwner(Long ownerId) {
        return vesselRepository.findByOwnerId(ownerId);
    }

    public Vessel getVesselById(Long id) {
        return vesselRepository.findById(id).orElse(null);
    }
}
