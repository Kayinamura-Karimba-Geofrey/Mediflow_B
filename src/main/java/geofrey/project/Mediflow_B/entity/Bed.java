package geofrey.project.Mediflow_B.entity;

import jakarta.persistence.*;
@Entity
@Table(name = "beds")
public class Bed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String bedNumber;

    private boolean isOccupied = false;

    @ManyToOne
    @JoinColumn(name = "ward_id")
    private Ward ward;

    public Bed() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }

    public boolean isOccupied() { return isOccupied; }
    public void setOccupied(boolean occupied) { isOccupied = occupied; }

    public Ward getWard() { return ward; }
    public void setWard(Ward ward) { this.ward = ward; }
}
