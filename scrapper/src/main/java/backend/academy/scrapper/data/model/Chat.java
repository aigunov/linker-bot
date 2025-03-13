package backend.academy.scrapper.data.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Getter
@Setter
@Table(name = "chat")
@AllArgsConstructor
@RequiredArgsConstructor
@Builder(toBuilder = true)
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name="tg_id")
    private Long tgId;

    @NotNull
    private String nickname;

    @OneToMany(mappedBy = "chat")
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "chat")
    private Set<Filter> filters = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "link_to_chat",
        joinColumns = {@JoinColumn(name = "chat_id")},
        inverseJoinColumns = {@JoinColumn(name = "link_id")}
    )
    private Set<Link> links = new HashSet<>();
}
