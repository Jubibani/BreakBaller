using UnityEngine;

public class BouncyBall : MonoBehaviour
{
    //reference to the RigidBody component of the bouncy ball object
    private Rigidbody2D rigidBody2D;
    public float maxVelocity = 15f;
    //line Y axis threshold below which if the ball passes this value, it reset to its position
    public float minimumY = -5.5f;

    int score = 0;
    int lives = 5;
    void Start()
    {
        //get the RigidBody2D component attached to the bouncy ball object
        rigidBody2D = GetComponent<Rigidbody2D>();

    }


    void Update()
    {

        ResetBall();
        ClampBallVelocity();
      
    }

    void ResetBall()
    {
        //check if the ball's Y position is below the minimum threshold
        if (transform.position.y < minimumY)
        {
            // decrease the life of the player
            lives--;

            //resets the ball's position to the center of the viewport
            transform.position = Vector3.zero;

            //stop the ball's movement by setting its velocity to zero
            rigidBody2D.linearVelocity = Vector3.zero;

        }
    }

    void ClampBallVelocity()
    {
        if (rigidBody2D.linearVelocity.magnitude > maxVelocity)
        {
            //clamp the magnitude of 'current velocity' of the ball object
            Vector3 clampedVelocity = Vector3.ClampMagnitude(rigidBody2D.linearVelocity, maxVelocity);

            //apply the clampedVelocity 
            rigidBody2D.linearVelocity = clampedVelocity;
        }

    }
    private void OnCollisionEnter2D(Collision2D collision)
    {   
        //checks if the object that the ball has collided with if it has the tag 'Brick'
        if (collision.gameObject.CompareTag("Brick"))
        {   
            Debug.Log("brick hit!");
            //destroys the object that the ball has collided with
            Destroy(collision.gameObject);

            //give score to player
            score += 10;
        }
    }
}
