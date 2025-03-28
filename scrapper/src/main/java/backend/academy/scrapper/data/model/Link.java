package backend.academy.scrapper.data.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "link")
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @URL
    private String url;

    @NotNull
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE},
        fetch = FetchType.EAGER)
    @JoinTable(
        name = "tag_to_link",
        joinColumns = {@JoinColumn(name="link_id")},
        inverseJoinColumns = {@JoinColumn(name="tag_id")}
    )
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE},
        fetch = FetchType.EAGER)
    @JoinTable(
        name = "link_to_filter",
        joinColumns = {@JoinColumn(name="link_id")},
        inverseJoinColumns = {@JoinColumn(name="filter_id")}
    )
    private Set<Filter> filters = new HashSet<>();

    @NotNull
    @ManyToMany(mappedBy = "links")
    private Set<Chat> chats;
}
