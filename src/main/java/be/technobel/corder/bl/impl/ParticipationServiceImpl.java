package be.technobel.corder.bl.impl;

import be.technobel.corder.bl.ParticipationService;
import be.technobel.corder.dl.models.Address;
import be.technobel.corder.dl.models.Participation;
import be.technobel.corder.dl.repositories.ParticipationRepository;
import be.technobel.corder.pl.config.exceptions.DuplicateParticipationException;
import be.technobel.corder.pl.models.forms.ParticipationForm;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParticipationServiceImpl implements ParticipationService {

    private final ParticipationRepository participationRepository;

    public ParticipationServiceImpl(ParticipationRepository participationRepository) {
        this.participationRepository = participationRepository;
    }

    private void isUniqueParticipant(Participation participation) {

        String email = formatEmail(participation);

        List<String> emails = findAll().stream()
                .map(this::formatEmail)
                .toList();

        if (emails.contains(email)) {
            throw new DuplicateParticipationException("Ce participant a déjà joué avec cet email !");
        }

        String address = participation.getAddress().toString();

        List<String> addresses = findAll().stream()
                .map(this::formatAddress)
                .toList();

        if (addresses.contains(address)) {
            throw new DuplicateParticipationException("Ce foyer a déjà une participation !");
        }
    }

    private String formatAddress(Participation participation) {
        Address Address = participation.getAddress();
        return (Address.getStreet() + Address.getCity() + Address.getPostCode()).trim().toLowerCase();
    }

    private String formatEmail(Participation participation) {
        return (participation.getEmail()).trim().toLowerCase();
    }

    @Override
    public Participation create(ParticipationForm participationForm) {
        isUniqueParticipant(participationForm.toEntity());
        return participationRepository.save(participationForm.toEntity());
    }

    @Override
    public List<Participation> findAll() {
        return participationRepository.findAll();
    }
}
