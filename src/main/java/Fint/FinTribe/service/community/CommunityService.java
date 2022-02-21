package Fint.FinTribe.service.community;

import Fint.FinTribe.domain.community.Community;
import Fint.FinTribe.repository.community.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommunityService {
    private final CommunityRepository communityRepository;

    public Community GetCommunity(ObjectId artId){
        return communityRepository.findByArtId(artId).get();
    }
}
